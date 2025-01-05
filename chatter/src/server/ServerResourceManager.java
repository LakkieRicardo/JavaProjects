package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Manages local server resources such as JSON files containing server configuration and data. Owned by a <code>ChatterServer</code>.
 */
public class ServerResourceManager {
    
    private final File resourceFolder;
    private final List<InetAddress> bannedUsers = new ArrayList<InetAddress>();

    public ServerResourceManager(String folderName) throws IOException {
        resourceFolder = new File(folderName);
        if (!resourceFolder.exists()) {
            resourceFolder.mkdir();
        }
        loadFiles();
    }

    private void loadFiles() throws IOException {
        JSONObject bannedUsersJSON;
        File bannedUsersFile = new File(resourceFolder, "banned-users.json");
        if (!bannedUsersFile.exists()) {
            bannedUsersFile.createNewFile();
            bannedUsersJSON = new JSONObject();
            bannedUsersJSON.put("banned_users", new JSONArray());
        } else {
            StringBuilder fileSourceBuilder = new StringBuilder();
            Scanner scanner = new Scanner(bannedUsersFile);
            while (scanner.hasNextLine()) {
                fileSourceBuilder.append(scanner.nextLine());
                fileSourceBuilder.append(System.lineSeparator());
            }
            scanner.close();
            bannedUsersJSON = new JSONObject(new String(fileSourceBuilder));
        }
        JSONArray bannedUsersArray = bannedUsersJSON.getJSONArray("banned_users");
        for (Object o : bannedUsersArray) {
            bannedUsers.add(InetAddress.getByName(o.toString()));
        }
    }

    public File getResourceFolder() {
        return resourceFolder;
    }

    public void writeOut() throws IOException {
        JSONObject bannedUsersOut = new JSONObject();
        JSONArray bannedArrayOut = new JSONArray();
        for (InetAddress bannedUser : bannedUsers) {
            bannedArrayOut.put(bannedUser.getHostName());
        }
        bannedUsersOut.put("banned_users", bannedArrayOut);
        FileWriter fileWriter = new FileWriter(new File(resourceFolder, "banned-users.json"));
        fileWriter.write(bannedUsersOut.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    public boolean addBannedUser(InetAddress address) {
        return bannedUsers.add(address);
    }

    public boolean removeBannedUser(InetAddress address) {
        return bannedUsers.remove(address);
    }

    public List<InetAddress> getBannedUsers() {
        return bannedUsers;
    }

}
