package io.bitbucket.pablo127.asanaexporter;

import io.bitbucket.pablo127.asanaexporter.model.UserMap;
import io.bitbucket.pablo127.asanaexporter.model.Workspace;
import io.bitbucket.pablo127.asanaexporter.model.user.User;
import io.bitbucket.pablo127.asanaexporter.model.user.UserData;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDownloadCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(UserDownloadCommand.class);

    @Getter
    private String workspaceId;

    @Getter
    private String userId;

    @Getter
    private String userName;
    
    @Getter
    private Map<String, String> allUsers = new HashMap<String, String>();

    @Getter
    private List<String> workspaceIds = new ArrayList<String>();

    @Override
    public void run() {
        try {
            Requester<User> requester = new Requester<>(User.class);
            System.out.println(requester.request(new UriBuilder().uri("https://app.asana.com/api/1.0/users/me")));
            UserData userData = requester.request(new UriBuilder().uri("https://app.asana.com/api/1.0/users/me"))
                    .getData();
        	
            // Get all the workspace IDs
			if(userData.getWorkspaces() != null && !userData.getWorkspaces().isEmpty()){
				for(Workspace workspace : userData.getWorkspaces()) {
	            	workspaceIds.add(workspace.getId());
	            }
			}
            this.workspaceId = workspaceIds.get(0);
            
            userId = userData.getGid();
            userName = userData.getName();
            Requester<UserMap> requesterWorkspace = new Requester<>(UserMap.class);
            
            // Get users of each workspace and add them
            for(String workspaceID : workspaceIds) {
            	List<HashMap<String, String>> userMap = requesterWorkspace.request(new UriBuilder().uri("https://app.asana.com/api/1.0/workspaces/" + workspaceID + "/users")).getData();
            	if(userMap != null && !userMap.isEmpty()) {
            		for(int i=0; i < userMap.size(); i++) {
            			String id = userMap.get(i).get("gid");
            			if(allUsers.get(id) == null){
            				allUsers.put(id, userMap.get(i).get("name"));
            			}
            		}
            	}
            }
           

            workspaceId = userData.getWorkspaces()
                    .get(0)
                    .getGid();
            userId = userData.getGid();
            userName = userData.getName();

            logger.info("Downloaded userData.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
