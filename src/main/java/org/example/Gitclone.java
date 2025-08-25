package org.example;





import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Gitclone {
    private static final String GIT_DIR = ".git";
    private static final String OBJECTS_DIR = GIT_DIR + "/objects";
    private static final String REFS_DIR = GIT_DIR + "/refs/heads";
    private static final String HEAD_FILE = GIT_DIR + "/HEAD";
    private static final String INDEX_FILE = GIT_DIR + "/index";
    private static final String GITIGNORE_FILE = ".gitignore";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java -jar gitclone.jar <command> [args]");
            return;
        }

        String command = args[0];
        switch (command) {
            case "init" -> init();
            case "add" -> {
                if (args.length > 1 && args[1].equals("-a")) {
                    addAll();
                } else if (args.length > 1) {
                    addFile(args[1]);
                } else {
                    System.out.println("Specify file(s) or use -a");
                }
            }
            case "commit" -> {
                if (args.length < 2) {
                    System.out.println("Commit message required");
                } else {
                    commit(args[1]);
                }
            }
            case "status" -> status();
            case "log" -> log();
            case "branch" -> {
                if (args.length == 1) listBranches();
                else createBranch(args[1]);
            }
            case "checkout" -> {
                if (args.length < 2) System.out.println("Branch or commit required");
                else checkout(args[1]);
            }
            default -> System.out.println("Unknown command: " + command);
        }
    }

    //  git init   commant
    private static void init() throws IOException {
        if (Files.exists(Paths.get(GIT_DIR))) {
            System.out.println("Repository already initialized.");
            return;
        }
        Files.createDirectories(Paths.get(OBJECTS_DIR));
        Files.createDirectories(Paths.get(REFS_DIR));
        Files.writeString(Paths.get(HEAD_FILE), "ref: refs/heads/main\n");
        Files.writeString(Paths.get(REFS_DIR + "/main"), "");
        System.out.println("Initialized empty Git repository");
    }

    // add commont
    private static void addFile(String filename) throws Exception {
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("File does not exist: " + filename);
            return;
        }
        if (isIgnored(filename)) {
            System.out.println("Ignored by .gitignore: " + filename);
            return;
        }

        byte[] content = Files.readAllBytes(f.toPath());
        String hash = sha1(content);
        Files.write(Paths.get(OBJECTS_DIR, hash), content);
        Files.writeString(Paths.get(INDEX_FILE), filename + " " + hash + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        System.out.println("Added file to staging: " + filename + " (" + hash + ")");
    }

    private static void addAll() throws Exception {
        File dir = new File(".");
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isFile() && !f.getName().equals("gitclone.jar")) {
                addFile(f.getName());
            }
        }
    }

    // commit
    private static void commit(String message) throws Exception {
        List<String> index = readLines(INDEX_FILE);
        if (index.isEmpty()) {
            System.out.println("Nothing to commit.");
            return;
        }

        StringBuilder tree = new StringBuilder();
        for (String line : index) tree.append(line).append("\n");

        String commitData = "tree\n" + tree + "message: " + message + "\n"
                + "date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String hash = sha1(commitData.getBytes());
        Files.write(Paths.get(OBJECTS_DIR, hash), commitData.getBytes());

        String branch = currentBranch();
        Files.writeString(Paths.get(REFS_DIR, branch), hash);

        new File(INDEX_FILE).delete();
        System.out.println("Committed: " + hash.substring(0, 7) + " - " + message);
    }

    // status feature
    private static void status() throws Exception {
        if (!Files.exists(Paths.get(INDEX_FILE))) {
            System.out.println("No files staged.");
            return;
        }
        List<String> index = readLines(INDEX_FILE);
        System.out.println("Staged files:");
        for (String line : index) System.out.println("  " + line);
    }

    // git log
    private static void log() throws Exception {
        String hash = Files.readString(Paths.get(REFS_DIR, currentBranch())).trim();
        while (!hash.isEmpty()) {
            String commitData = Files.readString(Paths.get(OBJECTS_DIR, hash));
            System.out.println("commit " + hash);
            for (String line : commitData.split("\n")) {
                if (line.startsWith("message:") || line.startsWith("date:"))
                    System.out.println("    " + line);
            }
            break; // simplified (no parent support yet)
        }
    }

    //   branch
    private static void listBranches() throws IOException {
        File refs = new File(REFS_DIR);
        for (File f : Objects.requireNonNull(refs.listFiles())) {
            String branch = f.getName();
            if (branch.equals(currentBranch())) System.out.println("* " + branch);
            else System.out.println("  " + branch);
        }
    }

    private static void createBranch(String name) throws IOException {
        String hash = Files.readString(Paths.get(REFS_DIR, currentBranch())).trim();
        Files.writeString(Paths.get(REFS_DIR, name), hash);
        System.out.println("Branch created: " + name);
    }

    private static void checkout(String name) throws IOException {
        File branchFile = new File(REFS_DIR, name);
        if (branchFile.exists()) {
            Files.writeString(Paths.get(HEAD_FILE), "ref: refs/heads/" + name + "\n");
            System.out.println("Switched to branch: " + name);
        } else {
            System.out.println("No such branch: " + name);
        }
    }

//       some helping methods
    private static boolean isIgnored(String filename) throws IOException {
        if (!Files.exists(Paths.get(GITIGNORE_FILE))) return false;
        List<String> ignores = readLines(GITIGNORE_FILE);
        return ignores.contains(filename);
    }

    private static String currentBranch() throws IOException {
        String head = Files.readString(Paths.get(HEAD_FILE)).trim();
        return head.replace("ref: refs/heads/", "");
    }

    private static String sha1(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static List<String> readLines(String path) throws IOException {
        if (!Files.exists(Paths.get(path))) return new ArrayList<>();
        return Files.readAllLines(Paths.get(path));
    }
}
