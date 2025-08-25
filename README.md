# GitClone – A Simple Git-like System in Java

GitClone is a lightweight, educational version-control system inspired by Git, implemented in **pure Java**.  
It supports basic commands such as `init`, `add`, `commit`, `log`, `status`, branching, rollback, and `.gitignore`.

---

## 🚀 Features
- `init` → Initialize a new repository (`.git` folder).
- `add <file>` → Stage a file for commit (checks if file exists).
- `add -a` → Stage **all files** in the working directory (respects `.gitignore`).
- `commit "<message>"` → Save staged files into a commit object.
- `log` → Show commit history.
- `status` → Show staged vs committed files.
- `branch <name>` → Create a new branch.
- `checkout <branch>` → Switch between branches.
- `rollback <commit-hash>` → Restore repository to a previous commit.
- `.gitignore` support → Skips files listed in `.gitignore`.

---

## 🛠️ Build Instructions

This is a **Maven project**. Make sure you have:
- Java 8+ installed
- Maven installed (`mvn -v`)

Build the project:
```sh
mvn clean package
java -jar target/gitclone-1.0-SNAPSHOT.jar <command> [args].


java -jar target/gitclone-1.0-SNAPSHOT.jar init
java -jar target/gitclone-1.0-SNAPSHOT.jar add hello.txt
java -jar target/gitclone-1.0-SNAPSHOT.jar commit "first commit"
java -jar target/gitclone-1.0-SNAPSHOT.jar log
java -jar target/gitclone-1.0-SNAPSHOT.jar branch feature-x
java -jar target/gitclone-1.0-SNAPSHOT.jar checkout feature-x
java -jar target/gitclone-1.0-SNAPSHOT.jar rollback <commit-hash>

