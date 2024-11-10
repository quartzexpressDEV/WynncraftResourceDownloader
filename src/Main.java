import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {
    private static final String WELCOME_MESSAGE = "This program will download the current (hopefully) Server Resource Pack for Wynncraft. During this process, it will connect to github.com, where the download links are hosted, as well as rp-cdn.wynncraft.com, where the Resource Pack is hosted.";
    private static final String READABLE_RP_TEXT = "Do you want to extract the files from the Resource Pack? This is useful if you want to edit the Resource Pack or view its contents, as by default this isn't possible";
    private static final String PACKS_JSON_URL = "https://raw.githubusercontent.com/quartzexpressDEV/wynncraft-resourcepacks/main/packs_new.json";
    // lol
    private static final Pattern jsonPattern = Pattern.compile("\"link\":[ \\t]*\"(.*)\"", Pattern.MULTILINE);
    public static void main(String[] args) {
        boolean Readable = false;

        System.out.println(WELCOME_MESSAGE);
        if (!ContinueYN("Do you want to continue")){
            System.out.println("Exiting program.");
            System.exit(0);
        }

        Readable = ContinueYN(READABLE_RP_TEXT);
        String VersionJson = DownloadString(PACKS_JSON_URL);
        if (VersionJson != null)
        {
            Matcher matcher = jsonPattern.matcher(VersionJson);
            if (matcher.find())
            {
                String RPUrl = matcher.group(1);
                String FileName = RPUrl.substring(RPUrl.lastIndexOf('/')+1);
                String FileNameNoExt = FileName.substring(0, FileName.lastIndexOf('.'));

                System.out.println("Downloading Resource Pack...");
                DownloadFileWithUserAgent(RPUrl, FileName, "Minecraft Java/1.20.4");
                System.out.println("Downloaded Resource Pack successfully as " + FileName);
                if (Readable) {
                    try {
                        System.out.println("Extracting Resource Pack...");
                        extractZip(new File(FileName), new File(FileNameNoExt));
                        System.out.println("Extracted Resource Pack successfully to ./" + FileNameNoExt);
                    } catch (IOException e) {
                        System.out.println("An error occurred while extracting the Resource Pack. Please report this with the information below:\n");
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
            }
            else
            {
                System.out.println("Epic regex error, please report this");
                System.exit(0);
            }
        } else {
            System.out.println("An Error occurred, this should not be possible, what the hell? Please report this");
            System.exit(1);
        }
    }
    public static boolean ContinueYN(String Message) {
        Scanner UserInputScanner = new Scanner(System.in);
        System.out.println(Message + " (y/n)?");
        String UserInput = UserInputScanner.nextLine();
        while (true) {
            if (UserInput.equals("y")) {
                return true;
            } else if (UserInput.equals("n")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
                UserInput = UserInputScanner.nextLine();
            }
        }

    }
    public static String DownloadString(String url) {
        try (Scanner scanner = new Scanner(new URI(url).toURL().openStream()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e){
            System.out.println("An error occurred while downloading a string. Please report this with the information below:\n");
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
    public static void DownloadFileWithUserAgent(String fileUrl, String path, String userAgent) {
        try {
            URL url = new URI(fileUrl).toURL();
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("User-Agent", userAgent);

            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(path);

                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
            } else throw new Exception("No file to download. Server replied HTTP code: " + responseCode);
            httpConn.disconnect();
        } catch (Exception e) {
            System.out.println("An error occurred while downloading a file. Please report this with the information below:\n");
            e.printStackTrace();
            System.exit(0);
        }

    }
    //yoinked this from mcrpx (https://github.com/Speedy11CZ/mcrpx) licensed under MIT license
    public static void extractZip(File file, File destDirectory) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdir();
        }

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.getName().contains("..")) {
                    continue;
                }

                try {
                    try (InputStream entryInputStream = zipFile.getInputStream(zipEntry)) {
                        String filePath = destDirectory + File.separator + zipEntry.getName();
                        File zipEntryFile = new File(filePath);
                        zipEntryFile.getParentFile().mkdirs();
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(zipEntryFile));
                        byte[] bytesIn = new byte[4096];
                        int read;
                        while ((read = entryInputStream.read(bytesIn)) != -1) {
                            bufferedOutputStream.write(bytesIn, 0, read);
                        }
                        bufferedOutputStream.close();
                    }
                } catch (IOException e) {
                    System.out.println("An error occurred while extracting a file. Please report this with the information below:\n");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }
    }
}