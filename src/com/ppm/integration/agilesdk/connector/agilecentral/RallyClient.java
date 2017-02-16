
package com.ppm.integration.agilesdk.connector.agilecentral;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;

import com.ppm.integration.agilesdk.connector.agilecentral.model.HierarchicalRequirement;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Iteration;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Project;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Release;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Subscription;
import com.ppm.integration.agilesdk.connector.agilecentral.model.TimeEntryItem;
import com.ppm.integration.agilesdk.connector.agilecentral.model.TimeEntryValue;
import com.ppm.integration.agilesdk.connector.agilecentral.model.User;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Workspace;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioFeature;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioInitiative;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioItem;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioTheme;
import com.ppm.integration.agilesdk.provider.Providers;
import com.ppm.integration.agilesdk.provider.UserProvider;

public class RallyClient {

    private final RestHelper helper;

    public RallyClient(String endpoint, Config config) {
        this.helper = new RestHelper(endpoint, config);
    }

    public Subscription getSubscription() {
        String subscriptionURI = "/slm/webservice/v2.0/subscription";
        return new Subscription(helper.get(subscriptionURI).getJSONObject("Subscription"));
    }

    public List<Workspace> getWorkspaces(Subscription subscription) {
        return getWorkspaces(subscription.getId());
    }

    public List<Workspace> getWorkspaces(String subscriptionId) {
        String workspacesURI = "/slm/webservice/v2.0/Subscription/?/Workspaces";
        workspacesURI = workspacesURI.replace("?", subscriptionId);
        JSONArray jsonArray = helper.getAll(workspacesURI);
        List<Workspace> workspaces = new ArrayList<Workspace>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            workspaces.add(new Workspace(jsonArray.getJSONObject(i)));
        }
        return workspaces;
    }

    public List<Project> getProjects(Workspace workspace) {
        return getProjects(workspace.getId());
    }

    public List<Project> getProjects(String workspaceId) {
        String projectsURI = "/slm/webservice/v2.0/Workspace/?/Projects";
        projectsURI = projectsURI.replace("?", workspaceId);
        JSONArray jsonArray = helper.getAll(projectsURI);
        List<Project> projects = new ArrayList<Project>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            projects.add(new Project(jsonArray.getJSONObject(i)));
        }
        return projects;
    }

    // Iteration
    public List<Iteration> getIterations(String projectId) {
        String iterationsURI = "/slm/webservice/v2.0/project/?/Iterations";
        iterationsURI = iterationsURI.replace("?", projectId);
        JSONArray jsonArray = helper.getAll(iterationsURI);
        List<Iteration> iterations = new ArrayList<Iteration>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            iterations.add(new Iteration(jsonArray.getJSONObject(i)));
        }
        return iterations;
    }

    public List<Iteration> getAllIterations(String projectId) {
        String iterationsURI = "/slm/webservice/v2.0/project/?/Iterations";
        iterationsURI = iterationsURI.replace("?", projectId);
        JSONArray jsonArray = helper.getAll(iterationsURI);
        List<Iteration> iterations = new ArrayList<Iteration>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            iterations.add(new Iteration(jsonArray.getJSONObject(i)));
        }
        // fill ExternalTasks
        List<HierarchicalRequirement> hierarchicalRequirements = getHierarchicalRequirements();
        fillHierarchicalRequirement(iterations, hierarchicalRequirements);
        List<User> users = getUsers();
        fillUser(hierarchicalRequirements, users);
        return iterations;
    }

    public Iteration getIteration(String projectId, String iterationId) {
        String iterationsURI = "/slm/webservice/v2.0/iteration/" + iterationId;
        Iteration iteration = new Iteration(helper.get(iterationsURI).getJSONObject("Iteration"));
        // fill ExternalTasks
        List<HierarchicalRequirement> hierarchicalRequirements = getHierarchicalRequirements();
        List<User> users = getUsers();
        fillUser(hierarchicalRequirements, users);
        for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {
            iteration.addHierarchicalRequirement(hierarchicalRequirement);
        }

        return iteration;
    }

    public List<HierarchicalRequirement> getHierarchicalRequirements() {
        String hierarchicalrequirementURI = "/slm/webservice/v2.0/hierarchicalrequirement";
        JSONArray jsonArray = helper.query(hierarchicalrequirementURI, "", true, "", 1, 20);
        List<HierarchicalRequirement> hierarchicalRequirements = new ArrayList<HierarchicalRequirement>();

        UserProvider userProvider = Providers.getUserProvider(RallyIntegrationConnector.class);
        for (int i = 0; i < jsonArray.size(); i++) {
            hierarchicalRequirements.add(new HierarchicalRequirement(jsonArray.getJSONObject(i), userProvider));
        }
        return hierarchicalRequirements;
    }

    private List<User> getUsers() {
        String userURI = "/slm/webservice/v2.0/user";
        JSONArray jsonArray = helper.query(userURI, "", true, "", 1, 20);
        List<User> users = new ArrayList<User>();
        for (int i = 0; i < jsonArray.size(); i++) {
            users.add(new User(jsonArray.getJSONObject(i)));
        }
        return users;
    }

    private void fillHierarchicalRequirement(List<Iteration> iterations,
            List<HierarchicalRequirement> hierarchicalRequirements)
    {
        for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {
            for (Iteration iteration : iterations) {
                iteration.addHierarchicalRequirement(hierarchicalRequirement);
            }
        }
    }

    private void fillUser(List<HierarchicalRequirement> hierarchicalRequirements, List<User> users) {
        for (User user : users) {
            for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {
                user.addHierarchicalRequirement(hierarchicalRequirement);
            }
        }
    }

    // Release
    public List<Release> getReleases(String projectId) {
        String releasesURI = "/slm/webservice/v2.0/project/?/Releases";
        releasesURI = releasesURI.replace("?", projectId);
        JSONArray jsonArray = helper.getAll(releasesURI);

        List<Release> releases = new ArrayList<Release>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            releases.add(new Release(jsonArray.getJSONObject(i)));
        }

        return releases;
    }

    public List<Release> getAllReleases(String projectId) {
        String releasesURI = "/slm/webservice/v2.0/project/?/Releases";
        releasesURI = releasesURI.replace("?", projectId);
        JSONArray jsonArray = helper.getAll(releasesURI);
        List<Release> releases = new ArrayList<Release>(jsonArray.size());

        List<HierarchicalRequirement> hierarchicalRequirements = getHierarchicalRequirements();
        List<User> users = getUsers();
        fillUser(hierarchicalRequirements, users);
        List<Iteration> iterations = getIterations(projectId);
        for (int i = 0; i < jsonArray.size(); i++) {
            Release release = new Release(jsonArray.getJSONObject(i));
            System.out.println("releaseName==" + release.getName());
            // fill ExternalTasks
            List<Iteration> thisIterations = getIterationsByRelease(iterations, release);
            fillHierarchicalRequirement(thisIterations, hierarchicalRequirements);
            for (Iteration iteration : thisIterations) {
                release.addIteration(iteration);
            }

            for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {
                if (hierarchicalRequirement.getIterationUUID() == null) {
                    release.addHierarchicalRequirement(hierarchicalRequirement);
                }
            }
            releases.add(release);
        }

        return releases;
    }

    public Release getRelease(String projectId, String releaseId) {
        String releasesURI = "/slm/webservice/v2.0/release/" + releaseId;
        Release release = new Release(helper.get(releasesURI).getJSONObject("Release"));

        List<HierarchicalRequirement> hierarchicalRequirements = getHierarchicalRequirements();
        List<User> users = getUsers();
        fillUser(hierarchicalRequirements, users);
        // fill ExternalTasks
        List<Iteration> iterations = getIterationsByRelease(getIterations(projectId), release);
        fillHierarchicalRequirement(iterations, hierarchicalRequirements);
        for (Iteration iteration : iterations) {
            release.addIteration(iteration);
        }

        for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {
            if (hierarchicalRequirement.getIterationUUID() == null) {
                release.addHierarchicalRequirement(hierarchicalRequirement);
            }
        }

        return release;
    }

    public List<Iteration> getIterationsByRelease(List<Iteration> iterations, Release release) {
        List<Iteration> thisIterations = new ArrayList<>();
        Date releaseStart = release.getScheduleStart();
        Date releaseEnd = release.getScheduleFinish();

        for (Iteration iteration : iterations) {
            Date iterationStart = iteration.getScheduleStart();
            if (iterationStart.getTime() > releaseStart.getTime() && iterationStart.getTime() < releaseEnd.getTime()) {
                thisIterations.add(iteration);
            }
        }

        return thisIterations;
    }

    // timesheet
    public HashMap<String, List<TimeEntryItem>> getTimeEntryItem() {
        HashMap<String, List<TimeEntryItem>> hms = new HashMap<>();
        String timeEntryItemURI = "/slm/webservice/v2.0/timeentryitem";
        JSONArray jsonArray = helper.query(timeEntryItemURI, "", true, "", 1, 20);

        for (int i = 0; i < jsonArray.size(); i++) {
            TimeEntryItem item = new TimeEntryItem(jsonArray.getJSONObject(i));
            String WorkProductUUID = item.getWorkProductUUID();
            if (hms.containsKey(item.getWorkProductUUID())) {
                List<TimeEntryItem> items = hms.get(item.getWorkProductUUID());
                items.add(item);
                hms.put(WorkProductUUID, items);
            } else {
                List<TimeEntryItem> items = new ArrayList<>();
                items.add(item);
                hms.put(WorkProductUUID, items);
            }
        }
        return hms;
    }

    public HashMap<String, List<TimeEntryValue>> getTimeEntryValue() {
        HashMap<String, List<TimeEntryValue>> hms = new HashMap<>();
        String timeEntryValueURI = "/slm/webservice/v2.0/timeentryvalue";
        JSONArray jsonArray = helper.query(timeEntryValueURI, "", true, "", 1, 20);
        for (int i = 0; i < jsonArray.size(); i++) {
            TimeEntryValue value = new TimeEntryValue(jsonArray.getJSONObject(i));
            String TimeEntryItemUUID = value.getTimeEntryItemUUID();
            if (hms.containsKey(TimeEntryItemUUID)) {
                List<TimeEntryValue> values = hms.get(TimeEntryItemUUID);
                values.add(value);
                hms.put(TimeEntryItemUUID, values);
            } else {
                List<TimeEntryValue> values = new ArrayList<>();
                values.add(value);
                hms.put(TimeEntryItemUUID, values);
            }
        }
        return hms;
    }

    // maybe useless
    public HashMap<String, List<HierarchicalRequirement>> getHierarchicalRequirement() {
        HashMap<String, List<HierarchicalRequirement>> hms = new HashMap<>();
        String HierarchicalRequirementURI = "/slm/webservice/v2.0/hierarchicalRequirement";
        JSONArray jsonArray = helper.query(HierarchicalRequirementURI, "", true, "", 1, 20);
        UserProvider userProvider = Providers.getUserProvider(RallyIntegrationConnector.class);

        for (int i = 0; i < jsonArray.size(); i++) {
            HierarchicalRequirement hierarchicalRequirement =
                    new HierarchicalRequirement(jsonArray.getJSONObject(i), userProvider);
            String IterationUUID = hierarchicalRequirement.getIterationUUID();
            if (hms.containsKey(hierarchicalRequirement.getIterationUUID())) {
                List<HierarchicalRequirement> hierarchicalRequirements =
                        hms.get(hierarchicalRequirement.getIterationUUID());
                hierarchicalRequirements.add(hierarchicalRequirement);
                hms.put(IterationUUID, hierarchicalRequirements);
            } else {
                List<HierarchicalRequirement> hierarchicalRequirements = new ArrayList<>();
                hierarchicalRequirements.add(hierarchicalRequirement);
                hms.put(IterationUUID, hierarchicalRequirements);
            }
        }
        return hms;
    }

    // workplan----PortfolioItem
    public HashMap<String, List<PortfolioItem>> getPortfolioItems(String projectId) {
        String portfolioItemURI = "/slm/webservice/v2.0/portfolioitem";
        JSONArray jsonArray = helper.getAll(portfolioItemURI);
        HashMap<String, List<PortfolioItem>> hms = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioItem item = new PortfolioTheme(jsonArray.getJSONObject(i));
            if (item.getProjectID().equals(projectId)) {
                String itemType = item.getType();
                if (hms.containsKey(itemType)) {
                    List<PortfolioItem> items = hms.get(itemType);
                    items.add(item);
                    hms.put(itemType, items);
                } else {
                    List<PortfolioItem> items = new ArrayList<>();
                    items.add(item);
                    hms.put(itemType, items);
                }
            }
        }
        return hms;
    }

    // theme
    public List<PortfolioTheme> getPortfolioThemes(String projectId) {
        String portfolioThemeURI = "/slm/webservice/v2.0/portfolioitem/theme";
        JSONArray jsonArray = helper.getAll(portfolioThemeURI);
        List<PortfolioTheme> themes = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioTheme theme = new PortfolioTheme(jsonArray.getJSONObject(i));
            if (theme.getProjectID().equals(projectId)) {
                themes.add(theme);
            }
        }
        return themes;
    }

    public List<PortfolioTheme> getAllPortfolioThemes(String projectId) {
        String portfolioThemeURI = "/slm/webservice/v2.0/portfolioitem/theme";
        JSONArray jsonArray = helper.getAll(portfolioThemeURI);
        List<PortfolioTheme> themes = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioTheme theme = new PortfolioTheme(jsonArray.getJSONObject(i));
            if (theme.getProjectID().equals(projectId)) {
                // fill ExternalTasks
                List<PortfolioInitiative> initiatives = getThemeInitiatives(theme.getId());
                for (PortfolioInitiative initiative : initiatives) {
                    theme.addInitiative(initiative);
                }
                themes.add(theme);
            }
        }
        return themes;
    }

    public PortfolioTheme getPortfolioTheme(String themeId) {
        String portfolioThemeURI = "/slm/webservice/v2.0/portfolioitem/theme/" + themeId;
        PortfolioTheme theme = new PortfolioTheme(helper.get(portfolioThemeURI).getJSONObject("Theme"));
        // fill ExternalTasks
        List<PortfolioInitiative> initiatives = getThemeInitiatives(themeId);
        for (PortfolioInitiative initiative : initiatives) {
            theme.addInitiative(initiative);
        }

        return theme;
    }

    public List<PortfolioInitiative> getThemeInitiatives(String themeId) {
        String usURI = "/slm/webservice/v2.0/portfolioitem/theme/" + themeId + "/Children";
        JSONArray jsonArray = helper.getAll(usURI);
        List<PortfolioInitiative> initiatives = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioInitiative initiative = new PortfolioInitiative(jsonArray.getJSONObject(i));
            // fill ExternalTasks
            List<PortfolioFeature> features = getInitiativeFeatures(initiative.getId());
            for (PortfolioFeature feature : features) {
                initiative.addFeature(feature);
            }

            initiatives.add(initiative);
        }
        return initiatives;
    }

    // initiative
    public List<PortfolioInitiative> getPortfolioInitiatives(String projectId) {
        String portfolioeURI = "/slm/webservice/v2.0/portfolioitem/initiative";
        JSONArray jsonArray = helper.getAll(portfolioeURI);
        List<PortfolioInitiative> initiatives = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioInitiative initiative = new PortfolioInitiative(jsonArray.getJSONObject(i));
            if (initiative.getProjectID().equals(projectId)) {
                initiatives.add(initiative);
            }
        }
        return initiatives;
    }

    public List<PortfolioInitiative> getAllPortfolioInitiatives(String projectId) {
        String portfolioeURI = "/slm/webservice/v2.0/portfolioitem/initiative";
        JSONArray jsonArray = helper.getAll(portfolioeURI);
        List<PortfolioInitiative> initiatives = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioInitiative initiative = new PortfolioInitiative(jsonArray.getJSONObject(i));
            if (initiative.getProjectID().equals(projectId)) {
                // fill ExternalTasks
                List<PortfolioFeature> features = getPortfolioFeatures(initiative.getId());
                for (PortfolioFeature feature : features) {
                    initiative.addFeature(feature);
                }
                initiatives.add(initiative);
            }
        }
        return initiatives;
    }

    public PortfolioInitiative getPortfolioInitiative(String initiativeId) {
        String portfolioInitiativeURI = "/slm/webservice/v2.0/portfolioitem/initiative/" + initiativeId;
        PortfolioInitiative initiative =
                new PortfolioInitiative(helper.get(portfolioInitiativeURI).getJSONObject("Initiative"));
        // fill ExternalTasks
        List<PortfolioFeature> features = getInitiativeFeatures(initiativeId);
        for (PortfolioFeature feature : features) {
            initiative.addFeature(feature);
        }

        return initiative;
    }

    public List<PortfolioFeature> getInitiativeFeatures(String initiativeId) {
        String usURI = "/slm/webservice/v2.0/portfolioitem/initiative/" + initiativeId + "/Children";
        JSONArray jsonArray = helper.getAll(usURI);
        List<PortfolioFeature> features = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioFeature feature = new PortfolioFeature(jsonArray.getJSONObject(i));
            // fill ExternalTasks
            List<HierarchicalRequirement> userStories = getFeatureUSs(feature.getId());
            for (HierarchicalRequirement userStory : userStories) {
                feature.addUserStory(userStory);
            }

            features.add(feature);
        }
        return features;
    }

    // feature
    public List<PortfolioFeature> getPortfolioFeatures(String projectId) {
        String portfolioThemeURI = "/slm/webservice/v2.0/portfolioitem/feature";
        JSONArray jsonArray = helper.getAll(portfolioThemeURI);
        List<PortfolioFeature> features = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioFeature feature = new PortfolioFeature(jsonArray.getJSONObject(i));
            if (feature.getProjectID().equals(projectId)) {
                features.add(feature);
            }
        }
        return features;
    }

    public List<PortfolioFeature> getAllPortfolioFeatures(String projectId) {
        String portfolioThemeURI = "/slm/webservice/v2.0/portfolioitem/feature";
        JSONArray jsonArray = helper.getAll(portfolioThemeURI);
        List<PortfolioFeature> features = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PortfolioFeature feature = new PortfolioFeature(jsonArray.getJSONObject(i));
            if (feature.getProjectID().equals(projectId)) {
                // fill ExternalTasks
                List<HierarchicalRequirement> userStories = getFeatureUSs(feature.getId());
                for (HierarchicalRequirement userStory : userStories) {
                    feature.addUserStory(userStory);
                }
                features.add(feature);
            }
        }
        return features;
    }

    public PortfolioFeature getPortfolioFeature(String featureId) {
        String portfolioFeatureURI = "/slm/webservice/v2.0/portfolioitem/feature/" + featureId;
        PortfolioFeature feature = new PortfolioFeature(helper.get(portfolioFeatureURI).getJSONObject("Feature"));
        // fill ExternalTasks
        List<HierarchicalRequirement> userStories = getFeatureUSs(featureId);
        for (HierarchicalRequirement userStory : userStories) {
            feature.addUserStory(userStory);
        }

        return feature;
    }

    public List<HierarchicalRequirement> getFeatureUSs(String featureId) {
        String usURI = "/slm/webservice/v2.0/portfolioitem/feature/" + featureId + "/UserStories";
        JSONArray jsonArray = helper.getAll(usURI);
        List<HierarchicalRequirement> userStories = new ArrayList<>();
        UserProvider userProvider = Providers.getUserProvider(RallyIntegrationConnector.class);
        for (int i = 0; i < jsonArray.size(); i++) {
            HierarchicalRequirement userStory = new HierarchicalRequirement(jsonArray.getJSONObject(i), userProvider);
            // fill ExternalTasks
            List<HierarchicalRequirement> hierarchicalRequirements = getChildrenOfUS(userStory.getId());
            for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {
                userStory.addHierarchicalRequirement(hierarchicalRequirement);
            }

            userStories.add(userStory);
        }
        List<User> users = getUsers();
        fillUser(userStories, users);
        return userStories;
    }

    // US
    public List<HierarchicalRequirement> getChildrenOfUS(String userStoryId) {
        List<HierarchicalRequirement> hierarchicalRequirements = new ArrayList<>();

        String URI = "/slm/webservice/v2.0/HierarchicalRequirement/" + userStoryId + "/Children";
        JSONArray jsonArray = helper.getAll(URI);
        UserProvider userProvider = Providers.getUserProvider(RallyIntegrationConnector.class);
        for (int i = 0; i < jsonArray.size(); i++) {
            HierarchicalRequirement hierarchicalRequirement =
                    new HierarchicalRequirement(jsonArray.getJSONObject(i), userProvider);

            if (hierarchicalRequirement.getChildrenCount() == 0) {
                hierarchicalRequirements.add(hierarchicalRequirement);
            } else {
                // fill ExternalTasks
                List<HierarchicalRequirement> USs = getChildrenOfUS(hierarchicalRequirement.getId());
                for (HierarchicalRequirement US : USs) {
                    hierarchicalRequirement.addHierarchicalRequirement(US);
                }

                hierarchicalRequirements.add(hierarchicalRequirement);
            }

        }

        List<User> users = getUsers();
        fillUser(hierarchicalRequirements, users);
        return hierarchicalRequirements;
    }
}
