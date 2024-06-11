package gg.minecrush.epicteams.database.filemanager;

import java.io.File;

public class FolderCreator {

    public static void createFolder(String basePath, String subfolder) {
        String folderPath = basePath + "/" + subfolder;
        File folder = new File(folderPath);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Folder created: " + folderPath);
            } else {
                System.out.println("Failed to create folder: " + folderPath);
            }
        } else {
            System.out.println("Folder already exists: " + folderPath);
        }
    }

    public static boolean deleteFolder(String basePath, String subfolder) {
        File folder = new File(basePath + "/" + subfolder);

        if (folder.exists()) {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteFolder(file.getAbsolutePath(), ""); // Corrected recursive call
                        } else {
                            file.delete();
                        }
                    }
                }
            }

            if (folder.delete()) {
                System.out.println("Folder deleted: " + folder.toString());
                return true;
            } else {
                System.out.println("Failed to delete folder: " + folder.toString());
                return false;
            }
        } else {
            System.out.println("Folder does not exist: " + folder.toString());
            return false;
        }
    }
}