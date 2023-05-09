package com.inspire.jira.workflow;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import org.apache.commons.lang.mutable.Mutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.CompositeName;

/**
 * This is the post-function class that gets executed at the end of the transition.
 * Any parameters that were saved in your factory class will be available in the transientVars Map.
 */
public class MyPostFunction extends AbstractJiraFunctionProvider {
    private static final Logger log = LoggerFactory.getLogger(MyPostFunction.class);
    public static final String FIELD_MESSAGE = "messageField";
    private JiraAuthenticationContext jiraAuthenticationContext;

    public MyPostFunction() {
        this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }


    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);
        String message = (String) transientVars.get(FIELD_MESSAGE);
//        System.out.println(transientVars);
//        System.out.println(getLoggedInUser().getName());
//        System.out.println("key sets: " + args.keySet());
//        System.out.println(args.get("messageField"));
//
//        issue.setAssignee(getLoggedInUser());
        String oldStatus = issue.getStatus().getName();
        String newStatus = (String) args.get("newStatus");

        if (!oldStatus.equals(newStatus)) {
            System.out.println("updating the description because the issue status was changes...");
            issue.setDescription("The status was updated from " + oldStatus + " to: " + newStatus + ".");
        }

        try {
            createSubJiraIssue(issue);
        } catch (CreateException e) {
            e.printStackTrace();
        }
    }


    private void createSubJiraIssue(MutableIssue parentIssue) throws CreateException {
        IssueFactory issueFactory = ComponentAccessor.getIssueFactory();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        MutableIssue newIssue = issueFactory.getIssue();
        newIssue.setParentId(parentIssue.getId());
        newIssue.setSummary("Newly Created Issue");
        newIssue.setProjectId(parentIssue.getProjectId());
        newIssue.setIssueTypeId(parentIssue.getIssueTypeId());
        newIssue.setAssignee(getLoggedInUser());
        newIssue.setDescription("This issue is a child of the issue *" + parentIssue + "*");
        issueManager.createIssueObject(currentUser, newIssue);

    }

    private ApplicationUser getLoggedInUser() {
        return jiraAuthenticationContext.getLoggedInUser();
    }
}