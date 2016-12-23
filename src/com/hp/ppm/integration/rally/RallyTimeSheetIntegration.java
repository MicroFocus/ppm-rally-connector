package com.hp.ppm.integration.rally;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.hp.ppm.integration.ValueSet;
import com.hp.ppm.integration.rally.model.Project;
import com.hp.ppm.integration.rally.model.Subscription;
import com.hp.ppm.integration.rally.model.Workspace;
import com.hp.ppm.integration.tm.IExternalWorkItem;
import com.hp.ppm.integration.tm.TimeSheetIntegration;
import com.hp.ppm.integration.tm.TimeSheetIntegrationContext;
import com.hp.ppm.integration.ui.Field;
import com.hp.ppm.integration.ui.PasswordText;
import com.hp.ppm.integration.ui.PlainText;
import com.hp.ppm.tm.model.TimeSheet;

public class RallyTimeSheetIntegration  implements TimeSheetIntegration{
	private final Logger logger = Logger.getLogger(this.getClass());

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//thread safe
	protected synchronized String convertDate(Date date){

		try {
			return dateFormat.format(date);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "";

	}
	@Override
	public List<Field> getMappingConfigurationFields(ValueSet paramValueSet) {
		return Arrays.asList(new Field[]{
				new PlainText(Constants.KEY_USERNAME,"USERNAME","",true),
				new PasswordText(Constants.KEY_PASSWORD,"PASSWORD","",true)
		});
	}

	private static ExecutorService workspaceFetcherService = Executors.newFixedThreadPool(2);
	private static ExecutorService projectFetcherService = Executors.newFixedThreadPool(2);
	private static ExecutorService releaseFetcherService = Executors.newFixedThreadPool(2);
	@Override
	public List<IExternalWorkItem> getExternalWorkItems(TimeSheetIntegrationContext context, final ValueSet values) {
		final List<IExternalWorkItem> items = getExternalWorkItemsByTasks(context, values);
		return items;
	}
	private List<IExternalWorkItem> getExternalWorkItemsByTasks(TimeSheetIntegrationContext context, final ValueSet values) {
		final List<IExternalWorkItem> items = Collections.synchronizedList(new LinkedList<IExternalWorkItem>());
		Config config = new Config();
		config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
		config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),values.get(Constants.KEY_PASSWORD));
		final RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL),config);
		final boolean passAuth = true;
		Subscription subscription = rallyClient.getSubscription();
		List<Workspace> workspace = rallyClient.getWorkspaces(subscription.getId());
		
		final List<Future<Boolean>> waitWorkspaceQueue = Collections.synchronizedList(new ArrayList<Future<Boolean>>(1));
		final List<Future<Boolean>> waitProjectQueue = Collections.synchronizedList(new ArrayList<Future<Boolean>>(workspace.size()));
		TimeSheet timeSheet = context.currentTimeSheet();

		final Date startDate = timeSheet.getPeriodStartDate().toGregorianCalendar().getTime();
		final Date endDate = timeSheet.getPeriodEndDate().toGregorianCalendar().getTime();
		
		for(final Workspace w : workspace){
			waitWorkspaceQueue.add(workspaceFetcherService.submit(new Callable<Boolean>(){

				@Override
				public Boolean call() throws Exception {
					List<Project> projects = new ArrayList<Project>();
					try {
						projects = rallyClient.getProjects(w.getId());
					}catch(Exception e) {
						logger.error(" error of getProjects in workspace [" + w.getName() + "]", e);
					}
					return null;
				}
				
			}));
		}
		
		return null;
	}

	

}
