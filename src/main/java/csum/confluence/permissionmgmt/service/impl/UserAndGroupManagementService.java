package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.user.search.page.Pager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UserAndGroupManagementService {

    protected Log log = LogFactory.getLog(this.getClass());

    protected CrowdService crowdService;

    @Autowired
    public UserAndGroupManagementService(CrowdService crowdService) {
        this.crowdService = crowdService;

        if (crowdService==null) {
			throw new RuntimeException("crowdService was not autowired in UserAndGroupManagementService");
        }
    }

    public User getUser(String username) {
        User user = null;
        try {
            user = crowdService.getUser(username);
        } catch (Throwable t) {
            log.error("Problem getting user '" + username + "'", t);
        }

        return user;
    }

    public User addUser(String userName, String email, String fullName) {
        User user = null;
        try {
            ImmutableUser.Builder userBuilder = new ImmutableUser.Builder();
            userBuilder.active(true);
            long directoryId = 0;
            userBuilder.directoryId(directoryId);
            userBuilder.displayName("John Doe");
            userBuilder.emailAddress("this.is.an@email.address");
            userBuilder.name("jdoe");
            user = userBuilder.toUser();
            String credential = null;
            user = crowdService.addUser(user, credential);
        } catch (Throwable t) {
            log.error("Problem creating user '" + userName + "'", t);
        }
        return user;
    }

    public Group getGroup(String groupName) {
        Group group = null;
        try {
            group = crowdService.getGroup(groupName);
        } catch (Throwable t) {
            log.error("Problem getting group '" + groupName + "'", t);
        }
        return group;
    }

    public Group addGroup(String groupName) {
        Group group = null;
        try {
            group = new ImmutableGroup(groupName);
            crowdService.addGroup(group);
            //Long directoryId = 0L;
            //GroupTemplate group = new GroupTemplate(groupName, directoryId);
            group = crowdService.addGroup(group);
        } catch (Throwable t) {
            log.error("Problem creating group '" + groupName + "'", t);
        }
        return group;
    }

    public void removeGroup(Group group) {
        if (group == null) {
            log.warn("Attempted to delete null group. Ignoring.");
        } else {
            try {
                crowdService.removeGroup(group);
            } catch (Throwable t) {
                log.error("Problem removing group with name '" + group.getName() + "'", t);
            }
        }
    }

    public Pager getMemberNames(Group group) {
        Pager pager = null;
        if (group == null) {
            log.warn("Attempted to get members of null group. Ignoring.");
        } else {
            try {
                pager = crowdService.getGroupWithAttributes(group);
            } catch (Throwable t) {
                log.error("Problem getting members of group '" + group.getName() + "'", t);
            }
        }
        return pager;
    }

    public void addMembership(Group group, User user) {
        if (group == null) {
            log.warn("Attempted to add user to null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to add null user to group. Ignoring.");
        } else {
            try {
                crowdService.addMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem adding user '" + user.getName() + "' to group '" + group.getName() + "'", t);
            }
        }
    }

    public void removeMembership(Group group, User user) {
        if (group == null) {
            log.warn("Attempted to remove user from null group. Ignoring.");
        } else if (user == null) {
            log.warn("Attempted to remove null user from group. Ignoring.");
        } else {
            try {
                crowdService.removeMembership(group, user);
            } catch (Throwable t) {
                log.error("Problem removing user '" + user.getName() + "' from group '" + group.getName() + "'", t);
            }
        }
    }
}
