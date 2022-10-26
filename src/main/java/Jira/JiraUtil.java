package Jira;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import org.joda.time.DateTime;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JiraUtil {
    /**
     * 登录JIRA并返回指定的JiraRestClient对象
     */
    public static JiraRestClient login_jira(String username, String password) throws URISyntaxException {
        try {
            final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
            final URI jiraServerUri = new URI("https://issues.apache.org/jira");
            final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, username,
                    password);
            return restClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取并返回指定的Issue对象
     */
    public static Issue get_issue(String issueNum, String username, String password) throws URISyntaxException {
        try {
            final JiraRestClient restClient = login_jira(username, password);
            final NullProgressMonitor pm = new NullProgressMonitor();
            final Issue issue = restClient.getIssueClient().getIssue(issueNum, pm);
            return issue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue的评论部分的内容
     */
    public static List<String> get_comments_body(Issue issue) throws URISyntaxException {
        try {
            List<String> comments = new ArrayList<String>();
            for (Comment comment : issue.getComments()) {
                comments.add(comment.getBody().toString());
            }
            return comments;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue的创建时间
     */
    public static DateTime get_create_time(Issue issue) throws URISyntaxException {
        try {
            return issue.getCreationDate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue的描述
     */
    public static String get_description(Issue issue) throws URISyntaxException {
        try {
            return issue.getDescription();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue的标题
     */
    public static String get_summary(Issue issue) throws URISyntaxException {
        try {
            return issue.getSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue的报告人的名字
     */
    public static String get_reporter(Issue issue) throws URISyntaxException {
        try {
            return issue.getReporter().getDisplayName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue的状态
     */
    public static String get_status(Issue issue) throws URISyntaxException {
        try {
            return issue.getStatus().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue的类型
     */
    public static String get_issue_type(Issue issue) throws URISyntaxException {
        try {
            return issue.getIssueType().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定JIRA issue 的模块
     */
    public static ArrayList<String> get_modules(Issue issue) throws URISyntaxException {
        try {
            ArrayList<String> arrayList = new ArrayList<String>();
            Iterator<BasicComponent> basicComponents = issue.getComponents().iterator();
            while (basicComponents.hasNext()) {
                String moduleName = basicComponents.next().getName();
                arrayList.add(moduleName);
            }
            return arrayList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取所有可以收集到的JIRA issue信息并返回JiraInfoModel类型对象
     */
    public static JiraInfoModel get_jira_info(Issue issue) throws URISyntaxException {
        List<String> jiraCommentsBody = get_comments_body(issue);
        DateTime jiraCreateTime = get_create_time(issue);
        String description = get_description(issue);
        String summary = get_summary(issue);
        String reporter = get_reporter(issue);
        String status = get_status(issue);
        String issueType = get_issue_type(issue);
        ArrayList<String> modules = get_modules(issue);
        JiraInfoModel jiraInfoModel = new JiraInfoModel();
        jiraInfoModel.setJiraCommentsBody(jiraCommentsBody);
        jiraInfoModel.setJiraCreateTime(jiraCreateTime);
        jiraInfoModel.setDescription(description);
        jiraInfoModel.setSummary(summary);
        jiraInfoModel.setReporter(reporter);
        jiraInfoModel.setStatus(status);
        jiraInfoModel.setIssueType(issueType);
        jiraInfoModel.setModules(modules);
        return jiraInfoModel;
    }
}