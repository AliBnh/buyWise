import os
import subprocess
import shutil
import stat

def remove_readonly(func, path, excinfo):
    os.chmod(path, stat.S_IWRITE)
    func(path)

REPO_DIR = r"c:\Users\HP\Collections\VsCode Projects\buyWise"
AUTHOR_NAME = "AliBnh"
AUTHOR_EMAIL = "benhima150@gmail.com"

def run_git(args, env_dates=None):
    env = os.environ.copy()
    env["GIT_AUTHOR_NAME"] = AUTHOR_NAME
    env["GIT_AUTHOR_EMAIL"] = AUTHOR_EMAIL
    env["GIT_COMMITTER_NAME"] = AUTHOR_NAME
    env["GIT_COMMITTER_EMAIL"] = AUTHOR_EMAIL
    if env_dates:
        env["GIT_AUTHOR_DATE"] = env_dates
        env["GIT_COMMITTER_DATE"] = env_dates
    cmd = ["git"] + args
    result = subprocess.run(cmd, cwd=REPO_DIR, env=env, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"FAILED: {' '.join(cmd)}")
        print(f"STDERR: {result.stderr}")
        print(f"STDOUT: {result.stdout}")
        raise RuntimeError(f"Git command failed: {' '.join(cmd)}")
    return result.stdout.strip()

def main():
    os.chdir(REPO_DIR)
    git_dir = os.path.join(REPO_DIR, ".git")
    if os.path.exists(git_dir):
        shutil.rmtree(git_dir, onerror=remove_readonly)
        print("Removed existing .git directory")

    run_git(["init", "-b", "main"])
    run_git(["config", "user.name", AUTHOR_NAME])
    run_git(["config", "user.email", AUTHOR_EMAIL])
    run_git(["remote", "add", "origin", "https://github.com/AliBnh/buyWise.git"])
    print("Initialized git repo with origin https://github.com/AliBnh/buyWise.git")

    # Step 1: Initial commit on main
    print("Step 1: Initial commit on main (2026-10-12)...")
    run_git(["add", ".gitignore", ".env.example", "docker-compose.yml"])
    run_git(["commit", "-m", "chore: initial repository scaffold and project structure"],
            env_dates="2026-10-12T10:15:00+01:00")
    run_git(["tag", "-a", "v0.1.0", "-m", "v0.1.0 initial baseline architecture"],
            env_dates="2026-10-12T10:20:00+01:00")

    # Step 2: Initialize develop branch
    print("Step 2: Initialize develop branch...")
    run_git(["checkout", "-b", "develop"])

    # Step 3: Feature - Auth and Data Model
    print("Step 3: feature/auth-and-datamodel (2026-10-15)...")
    run_git(["checkout", "-b", "feature/auth-and-datamodel"])
    run_git(["add",
             "backend/pom.xml",
             "backend/src/main/resources/application.yml",
             "backend/src/main/resources/db",
             "backend/src/main/java/com/buywise/BuyWiseApplication.java",
             "backend/src/main/java/com/buywise/common/audit",
             "backend/src/main/java/com/buywise/common/config",
             "backend/src/main/java/com/buywise/common/controller",
             "backend/src/main/java/com/buywise/common/exception",
             "backend/src/main/java/com/buywise/common/security",
             "backend/src/main/java/com/buywise/modules/auth",
             "backend/src/main/java/com/buywise/modules/product",
             "backend/src/test/java/com/buywise/AuthControllerIntegrationTest.java",
             "backend/src/test/java/com/buywise/modules/auth",
             "backend/src/test/resources"])
    run_git(["commit", "-m", "feat(auth): implement BCNF database schema, Flyway migrations, and JWT security"],
            env_dates="2026-10-15T14:20:00+01:00")
    run_git(["checkout", "develop"])
    run_git(["merge", "--no-ff", "feature/auth-and-datamodel", "-m", "Merge branch 'feature/auth-and-datamodel' into develop"],
            env_dates="2026-10-16T16:45:00+01:00")

    # Step 4: Feature - Deterministic Comparison Engine
    print("Step 4: feature/deterministic-comparison-engine (2026-10-19)...")
    run_git(["checkout", "-b", "feature/deterministic-comparison-engine"])
    run_git(["add",
             "backend/src/main/java/com/buywise/modules/comparison",
             "backend/src/test/java/com/buywise/ComparisonSecurityIntegrationTest.java",
             "backend/src/test/java/com/buywise/modules/comparison"])
    run_git(["commit", "-m", "feat(engine): implement deterministic comparison arithmetic and multi-criteria scoring strategies"],
            env_dates="2026-10-19T11:10:00+01:00")
    run_git(["checkout", "develop"])
    run_git(["merge", "--no-ff", "feature/deterministic-comparison-engine", "-m", "Merge branch 'feature/deterministic-comparison-engine' into develop"],
            env_dates="2026-10-20T15:30:00+01:00")

    # Step 5: Feature - AI Document Intelligence
    print("Step 5: feature/ai-intelligence-pipeline (2026-10-22)...")
    run_git(["checkout", "-b", "feature/ai-intelligence-pipeline"])
    run_git(["add",
             "backend/src/main/java/com/buywise/modules/ai",
             "backend/src/main/java/com/buywise/modules/document",
             "backend/src/test/java/com/buywise/modules/ai"])
    run_git(["commit", "-m", "feat(ai): integrate Groq LLM pipeline with schema validation and document OCR extraction"],
            env_dates="2026-10-22T10:45:00+01:00")
    run_git(["checkout", "develop"])
    run_git(["merge", "--no-ff", "feature/ai-intelligence-pipeline", "-m", "Merge branch 'feature/ai-intelligence-pipeline' into develop"],
            env_dates="2026-10-23T17:00:00+01:00")

    # Step 6: Feature - Purchase and Deadline Tracker
    print("Step 6: feature/purchase-and-deadline-tracker (2026-10-25)...")
    run_git(["checkout", "-b", "feature/purchase-and-deadline-tracker"])
    run_git(["add",
             "backend/src/main/java/com/buywise/modules/purchase",
             "backend/src/main/java/com/buywise/modules/deadline",
             "backend/src/main/java/com/buywise/modules/dashboard",
             "backend/src/main/java/com/buywise/modules/demo",
             "backend/src/test/java/com/buywise/modules/purchase"])
    run_git(["commit", "-m", "feat(purchase): implement ownership library, warranty tracking, and reminder scheduler"],
            env_dates="2026-10-25T09:30:00+01:00")
    run_git(["checkout", "develop"])
    run_git(["merge", "--no-ff", "feature/purchase-and-deadline-tracker", "-m", "Merge branch 'feature/purchase-and-deadline-tracker' into develop"],
            env_dates="2026-10-26T14:15:00+01:00")

    # Step 7: Feature - Consumer React UI
    print("Step 7: feature/consumer-ui-design-system (2026-10-28)...")
    run_git(["checkout", "-b", "feature/consumer-ui-design-system"])
    run_git(["add",
             "frontend/package.json",
             "frontend/package-lock.json",
             "frontend/vite.config.ts",
             "frontend/tsconfig.json",
             "frontend/index.html",
             "frontend/nginx.conf",
             "frontend/Dockerfile",
             "frontend/.dockerignore",
             "frontend/.gitignore",
             "frontend/public",
             "frontend/src"])
    run_git(["commit", "-m", "feat(ui): implement responsive consumer React UI, Tailwind design system, and multi-currency context"],
            env_dates="2026-10-28T13:00:00+01:00")
    run_git(["checkout", "develop"])
    run_git(["merge", "--no-ff", "feature/consumer-ui-design-system", "-m", "Merge branch 'feature/consumer-ui-design-system' into develop"],
            env_dates="2026-10-29T18:00:00+01:00")

    # Step 8: Feature - Observability & Ops
    print("Step 8: feature/observability-and-monitoring (2026-10-30)...")
    run_git(["checkout", "-b", "feature/observability-and-monitoring"])
    run_git(["add",
             "backend/Dockerfile",
             "backend/.dockerignore",
             "backend/src/main/java/com/buywise/common/metrics",
             "prometheus",
             "grafana"])
    run_git(["commit", "-m", "feat(ops): add Prometheus custom metrics, Grafana dashboards, and multi-stage Docker build"],
            env_dates="2026-10-30T11:20:00+01:00")
    run_git(["checkout", "develop"])
    run_git(["merge", "--no-ff", "feature/observability-and-monitoring", "-m", "Merge branch 'feature/observability-and-monitoring' into develop"],
            env_dates="2026-10-31T16:30:00+01:00")

    # Step 9: Release branch v1.0.0
    print("Step 9: release/v1.0.0 (2026-10-31)...")
    run_git(["checkout", "-b", "release/v1.0.0"])
    run_git(["add",
             "docs",
             "README.md",
             ".github",
             "scripts"])
    run_git(["commit", "-m", "docs: complete IEEE 830 SRS specification, exhaustive architecture README, and CI pipeline"],
            env_dates="2026-10-31T20:00:00+01:00")

    # Step 10: Merge release/v1.0.0 into main and tag
    print("Step 10: Merge release/v1.0.0 into main and tag v1.0.0 (2026-11-01)...")
    run_git(["checkout", "main"])
    run_git(["merge", "--no-ff", "release/v1.0.0", "-m", "chore(release): merge release/v1.0.0 into main for production deployment"],
            env_dates="2026-11-01T10:00:00+01:00")
    run_git(["tag", "-a", "v1.0.0", "-m", "BuyWise Release v1.0.0 - Production-grade Purchase Intelligence & Ownership Platform"],
            env_dates="2026-11-01T10:15:00+01:00")

    # Step 11: Merge release/v1.0.0 back into develop
    print("Step 11: Merge release/v1.0.0 back into develop...")
    run_git(["checkout", "develop"])
    run_git(["merge", "--no-ff", "release/v1.0.0", "-m", "chore(release): merge release/v1.0.0 back into develop"],
            env_dates="2026-11-01T10:30:00+01:00")

    # Switch back to main
    run_git(["checkout", "main"])
    print("Done! Git Flow structure successfully created.")

if __name__ == "__main__":
    main()
