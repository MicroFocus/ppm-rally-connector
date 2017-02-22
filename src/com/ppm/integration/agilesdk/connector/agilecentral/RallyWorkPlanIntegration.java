
package com.ppm.integration.agilesdk.connector.agilecentral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Iteration;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Project;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Release;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Subscription;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Workspace;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioFeature;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioInitiative;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioItem;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioTheme;
import com.ppm.integration.agilesdk.connector.agilecentral.ui.RallyEntityDropdown;
import com.ppm.integration.agilesdk.pm.ExternalTask;
import com.ppm.integration.agilesdk.pm.ExternalWorkPlan;
import com.ppm.integration.agilesdk.pm.WorkPlanIntegration;
import com.ppm.integration.agilesdk.pm.WorkPlanIntegrationContext;
import com.ppm.integration.agilesdk.ui.Field;
import com.ppm.integration.agilesdk.ui.PasswordText;
import com.ppm.integration.agilesdk.ui.PlainText;

public class RallyWorkPlanIntegration extends WorkPlanIntegration {
    String strTemp = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<Field> getMappingConfigurationFields(WorkPlanIntegrationContext context, final ValueSet values) {
        return Arrays.asList(new Field[] {new PlainText(Constants.KEY_USERNAME, "USERNAME", "dan@acme.com", true),
                new PasswordText(Constants.KEY_PASSWORD, "PASSWORD", "Release!", true),
                new RallyEntityDropdown(Constants.KEY_SUBSCRIPTION, "SUBSCRIPTION", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_USERNAME, Constants.KEY_PASSWORD});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        Subscription subscription = rallyClient.getSubscription();
                        return Arrays.asList(new Option[] {new Option(subscription.getId(), subscription.getName())});
                    }

                }, new RallyEntityDropdown(Constants.KEY_WORKSPACE, "WORKSPACE", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_SUBSCRIPTION});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        for (Workspace w : rallyClient.getWorkspaces(values.get(Constants.KEY_SUBSCRIPTION))) {
                            options.add(new Option(w.getId(), w.getName()));
                        }

                        return options;
                    }

                }, new RallyEntityDropdown(Constants.KEY_PROJECT, "PROJECT", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_WORKSPACE});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        for (Project p : rallyClient.getProjects(values.get(Constants.KEY_WORKSPACE))) {
                            int count = p.getChildrenCount();
                            if (count > 0) {
                                options.add(new Option(p.getId(), Constants.KEY_PARENT_PROJECT_LABEL + " "
                                        + p.getName()));
                            } else {
                                options.add(new Option(p.getId(), p.getName()));
                            }
                        }

                        return options;
                    }

                }, new RallyEntityDropdown(Constants.KEY_LEVEL_DDL, "Level_to_Synchronous", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_PROJECT});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        options.add(new Option(Constants.KEY_LEVEL_ITERATION, "Iteration WorkItems"));
                        options.add(new Option(Constants.KEY_LEVEL_RELEASE, "Release WorkItems"));
                        HashMap<String, List<PortfolioItem>> hms =
                                rallyClient.getPortfolioItems(values.get(Constants.KEY_PROJECT));
                        for (String key : hms.keySet()) {
                            options.add(new Option(key, key + " WorkItems"));
                        }
                        return options;
                    }
                }, new RallyEntityDropdown(Constants.KEY_ITERATION_DDL, "Data_Detail", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_LEVEL_DDL, Constants.KEY_PROJECT});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        String Level = values.get(Constants.KEY_LEVEL_DDL);
                        switch (Level) {
                            case Constants.KEY_LEVEL_ITERATION:
                                options.add(new Option(Constants.KEY_ALL_ITEMS, "All Iterations"));
                                List<Iteration> iterations =
                                        rallyClient.getIterations(values.get(Constants.KEY_PROJECT));
                                for (Iteration iteration : iterations) {
                                    options.add(new Option(iteration.getId(), iteration.getName()));
                                }
                                break;
                            case Constants.KEY_LEVEL_RELEASE:
                                options.add(new Option(Constants.KEY_ALL_ITEMS, "All Releases"));
                                List<Release> releases = rallyClient.getReleases(values.get(Constants.KEY_PROJECT));
                                for (Release release : releases) {
                                    options.add(new Option(release.getId(), release.getName()));
                                }
                                break;
                            case Constants.KEY_LEVEL_THEME:
                                options.add(new Option(Constants.KEY_ALL_ITEMS, "All Themes"));
                                List<PortfolioTheme> portfolioThemes =
                                        rallyClient.getPortfolioThemes(values.get(Constants.KEY_PROJECT));
                                for (PortfolioTheme portfolioTheme : portfolioThemes) {
                                    options.add(new Option(portfolioTheme.getId(), portfolioTheme.getName()));
                                }
                                break;
                            case Constants.KEY_LEVEL_INITIATIVE:
                                options.add(new Option(Constants.KEY_ALL_ITEMS, "All Initiatives"));
                                List<PortfolioInitiative> portfolioInitiatives =
                                        rallyClient.getPortfolioInitiatives(values.get(Constants.KEY_PROJECT));
                                for (PortfolioInitiative portfolioInitiative : portfolioInitiatives) {
                                    options.add(new Option(portfolioInitiative.getId(), portfolioInitiative.getName()));
                                }
                                break;
                            case Constants.KEY_LEVEL_FEATURE:
                                options.add(new Option(Constants.KEY_ALL_ITEMS, "All features"));
                                List<PortfolioFeature> portfolioFeatures =
                                        rallyClient.getPortfolioFeatures(values.get(Constants.KEY_PROJECT));
                                for (PortfolioFeature portfolioFeature : portfolioFeatures) {
                                    options.add(new Option(portfolioFeature.getId(), portfolioFeature.getName()));
                                }
                                break;
                        }

                        return options;
                    }
                }});
    }

    @Override
    public ExternalWorkPlan getExternalWorkPlan(WorkPlanIntegrationContext context, ValueSet values) {

        Config config = new Config();
        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME), values.get(Constants.KEY_PASSWORD));
        final RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

        final String projectId = values.get(Constants.KEY_PROJECT);
        final String levelDDL = values.get(Constants.KEY_LEVEL_DDL);
        final String iterationDDL = values.get(Constants.KEY_ITERATION_DDL);
        ExternalWorkPlan externalWorkPlan = new ExternalWorkPlan() {

            @Override
            public List<ExternalTask> getRootTasks() {
                List<ExternalTask> externalTasks = new ArrayList<ExternalTask>();
                switch (levelDDL) {
                    case Constants.KEY_LEVEL_ITERATION:
                        if (!iterationDDL.equals(Constants.KEY_ALL_ITEMS)) {
                            externalTasks.add(rallyClient.getIteration(projectId, iterationDDL));
                        } else {
                            for (Iteration iteration : rallyClient.getAllIterations(projectId)) {
                                externalTasks.add(iteration);
                            }
                        }

                        break;
                    case Constants.KEY_LEVEL_RELEASE:
                        if (!iterationDDL.equals(Constants.KEY_ALL_ITEMS)) {
                            externalTasks.add(rallyClient.getRelease(projectId, iterationDDL));
                        } else {
                            for (Release release : rallyClient.getAllReleases(projectId)) {
                                externalTasks.add(release);
                            }
                        }

                        break;
                    case Constants.KEY_LEVEL_THEME:
                        if (!iterationDDL.equals(Constants.KEY_ALL_ITEMS)) {
                            externalTasks.add(rallyClient.getPortfolioTheme(iterationDDL));
                        } else {
                            for (PortfolioTheme theme : rallyClient.getAllPortfolioThemes(projectId)) {
                                externalTasks.add(theme);
                            }
                        }

                        break;
                    case Constants.KEY_LEVEL_INITIATIVE:
                        if (!iterationDDL.equals(Constants.KEY_ALL_ITEMS)) {
                            externalTasks.add(rallyClient.getPortfolioInitiative(iterationDDL));
                        } else {
                            for (PortfolioInitiative initiative : rallyClient.getAllPortfolioInitiatives(projectId)) {
                                externalTasks.add(initiative);
                            }
                        }

                        break;
                    case Constants.KEY_LEVEL_FEATURE:
                        if (!iterationDDL.equals(Constants.KEY_ALL_ITEMS)) {
                            externalTasks.add(rallyClient.getPortfolioFeature(iterationDDL));
                        } else {
                            for (PortfolioFeature feature : rallyClient.getAllPortfolioFeatures(projectId)) {
                                externalTasks.add(feature);
                            }
                        }

                        break;
                }

                return externalTasks;
            }
        };
        return externalWorkPlan;
    }

    @Override
    public String getCustomDetailPage() {
        return "/itg/integrationcenter/agm-connector-impl-web/agm-graphs.jsp";
    }

}
