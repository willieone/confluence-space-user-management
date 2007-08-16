/**
 * Copyright (c) 2007, Custom Space Usergroups Manager Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space Usergroups Manager Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.service.impl;

import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.Group;
import csum.confluence.permissionmgmt.service.exception.AddException;
import csum.confluence.permissionmgmt.service.exception.RemoveException;
import csum.confluence.permissionmgmt.service.vo.ServiceContext;
import csum.confluence.permissionmgmt.util.StringUtil;
import csum.confluence.permissionmgmt.util.group.GroupNameUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Rajendra Kadam
 * @author Gary S. Weaver
 */
public class ConfluenceGroupManagementService extends BaseGroupManagementService {

    public void addGroups(List groupNames, ServiceContext context) throws AddException {
        log.debug("addGroups() called. groupName='" + StringUtil.convertCollectionToCommaDelimitedString(groupNames) + "'");
        Space space = context.getSpace();

        List success = new ArrayList();
        List alreadyExisted = new ArrayList();

        for (int i = 0; i < groupNames.size(); i++) {
            String groupName = (String) groupNames.get(i);
            if (userAccessor.getGroup(groupName) == null) {

                Group vGroup = userAccessor.addGroup(groupName);
                log.debug("created " + groupName);
                success.add(groupName);

                //If group exists then set all required permissions
                if (vGroup != null) {
                    SpacePermission perm = new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION, space, vGroup.getName());
                    space.addPermission(perm);
                    log.debug("added viewspace perm to " + groupName);
                }
            } else {
                alreadyExisted.add(groupName);
            }
        }

        if (alreadyExisted.size() > 0) {
            String msg = "";
            String concat = "";
            if (alreadyExisted.size() > 0) {
                msg += context.getText("groups.already.existed") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(alreadyExisted) + ".";
                concat = " ";
            }

            if (success.size() > 0) {
                msg += concat;
                msg += context.getText("error.groupAddSuccess") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(success) + ".";
            }

            throw new AddException(msg);
        }
    }

    public void removeGroups(List groupNames, ServiceContext context) throws RemoveException {
        log.debug("removeGroup() called. groupNames are " + StringUtil.convertCollectionToCommaDelimitedString(groupNames));
        List didNotExist = new ArrayList();
        List badGroupNames = new ArrayList();
        List success = new ArrayList();

        //Remove Selected Groups
        for (Iterator iterator = groupNames.iterator(); iterator.hasNext();) {
            String grpName = (String) iterator.next();
            Pattern pat = GroupNameUtil.createGroupMatchingPattern(getCustomPermissionConfiguration(), context.getSpace().getKey());
            boolean isPatternMatch = GroupNameUtil.doesGroupMatchPattern(grpName, pat);

            // Space admin should not be able to delete any groups whose names begin with "confluence"
            if (!grpName.startsWith("confluence") && isPatternMatch) {
                Group group = userAccessor.getGroup(grpName);
                if (group != null) {
                    userAccessor.removeGroup(group);
                    success.add(grpName);
                } else {
                    didNotExist.add(grpName);
                }
            } else {
                log.debug("Not deleting group '" + grpName + "', as either it started with 'confluence' or didn't match pattern " + pat.pattern());
                badGroupNames.add(grpName);
            }
        }

        // if we failed, throw exception
        if (badGroupNames.size() > 0 || didNotExist.size() > 0) {
            String msg = "";
            String concat = "";
            if (badGroupNames.size() > 0) {
                msg += context.getText("error.badGroupNames") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(badGroupNames) + ".";
                concat = " ";
            }

            if (didNotExist.size() > 0) {
                msg += concat;
                msg += context.getText("error.groupsDidNotExist") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(didNotExist) + ".";
            }

            if (success.size() > 0) {
                msg += concat;
                msg += context.getText("error.groupRemoveSuccess") + ": " +
                        StringUtil.convertCollectionToCommaDelimitedString(success) + ".";
            }
            throw new RemoveException(msg);
        }
    }
}