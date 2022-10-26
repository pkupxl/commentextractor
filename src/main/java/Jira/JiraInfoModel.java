package Jira;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * 代表一个Issue报告
 */
public class JiraInfoModel {
    List<String> jiraCommentsBody;
    DateTime jiraCreateTime;
    String description;
    String summary;
    String reporter;
    String status;
    String issueType;
    ArrayList<String> modules;

    public List<String> getJiraCommentsBody() {
        return jiraCommentsBody;
    }

    public void setJiraCommentsBody(List<String> jiraCommentsBody) {
        this.jiraCommentsBody = jiraCommentsBody;
    }

    public DateTime getJiraCreateTime() {
        return jiraCreateTime;
    }

    public void setJiraCreateTime(DateTime jiraCreateTime) {
        this.jiraCreateTime = jiraCreateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public ArrayList<String> getModules() {
        return modules;
    }

    public void setModules(ArrayList<String> modules) {
        this.modules = modules;
    }
}
