# 🤝 Contributing Guide

Read this before you push anything. It keeps the repo clean and avoids painful merge conflicts.

---

## ✅ Project Setup Checklist

Use this when starting the project. Check off each item as a team.

### One-time setup (do this together in week 1)
- [ ] Create the GitHub repository
- [ ] Add all team members as collaborators (Settings → Collaborators)
- [ ] Protect `main` branch (Settings → Branches → Add rule → require 1 review before merging)
- [ ] Create a `dev` branch from `main`
- [ ] Protect `dev` branch the same way (optional but recommended)
- [ ] Everyone clones the repo locally
- [ ] Everyone copies `.env.example` → `.env` and fills in their values
- [ ] Connect Slack notifications (see section below)
- [ ] Agree on who reviews whose PRs

### Per-feature checklist (every time you work on something)
- [ ] Pull latest `dev` before starting
- [ ] Create a new branch from `dev`
- [ ] Write your code
- [ ] Test it locally
- [ ] Push and open a Pull Request into `dev`
- [ ] Request a review from a teammate
- [ ] Fix review comments if any
- [ ] Merge after approval

---

## 🌿 Branching Strategy

We use a simple 3-level model:

```
main                        ← stable, always works (protected)
 └── dev                    ← integration branch (merge features here)
      └── feature/name-xyz  ← your daily work
```

**Rules:**
- ❌ Never push directly to `main` or `dev`
- ✅ Always branch off `dev`
- ✅ Always merge back into `dev` via a Pull Request
- `main` only gets updated at milestones (e.g. end of sprint, demo day)

---

## 🔀 How to Create a Branch

```bash
# 1. Switch to dev and get the latest changes
git checkout dev
git pull origin dev

# 2. Create your feature branch
git checkout -b feature/your-name-short-description

# Examples:
# git checkout -b feature/alice-login-page
# git checkout -b feature/bob-database-schema
# git checkout -b fix/carol-crash-on-empty-form
```

**Branch naming convention:**

| Prefix | When to use |
|--------|-------------|
| `feature/` | New feature or page |
| `fix/` | Bug fix |
| `refactor/` | Code cleanup, no new behavior |
| `docs/` | Documentation only |
| `chore/` | Config, dependencies, tooling |

---

## 💾 Committing

Keep commits small and meaningful. Write messages in the imperative mood:

```bash
git add .
git commit -m "add login form validation"
git push origin feature/alice-login-page
```

**Good commit messages:**
```
✅ add user authentication
✅ fix crash when input is empty
✅ update database schema for users table
```

**Bad commit messages:**
```
❌ stuff
❌ WIP
❌ fixed things
❌ aaaaaa
```

---

## 🔁 How to Open a Pull Request (PR)

1. Push your branch to GitHub:
   ```bash
   git push origin feature/your-name-description
   ```

2. Go to the repo on **GitHub.com**

3. Click the **"Compare & pull request"** button that appears at the top

4. Fill in the PR form:
   - **Title:** Short summary (e.g. `Add login page`)
   - **Description:** What did you change and why? Any notes for the reviewer?
   - **Reviewers:** Assign at least 1 teammate (top right panel)

5. Click **"Create pull request"**

6. Wait for a review. Fix any comments, then **merge** once approved.

> 💡 PRs don't need to be perfect — they just need to be readable and working.

---

## 👀 How to Review a PR

1. Go to the PR on GitHub
2. Click **"Files changed"** to see what changed
3. Leave comments by clicking the `+` next to a line
4. When done, click **"Review changes"** → choose:
   - ✅ **Approve** — looks good, merge it
   - 💬 **Comment** — just leaving notes, no decision
   - ❌ **Request changes** — needs fixes before merging
5. The author fixes the comments, you approve, then they merge

---

## 🔔 Connecting GitHub to Slack

Get notified in Slack when someone pushes, opens a PR, or merges — without constantly checking GitHub.

### Step 1 — Add the GitHub app to Slack

1. In Slack, go to **Apps** (left sidebar) → search **GitHub**
2. Click **Add to Slack** → follow the install steps
3. Authenticate with your GitHub account when prompted

### Step 2 — Subscribe a Slack channel to your repo

In the Slack channel where you want notifications (e.g. `#project-updates`):

```
/github subscribe your-org/your-repo-name
```

Replace `your-org/your-repo-name` with your actual GitHub org and repo name.

### Step 3 — Choose what you get notified about

By default you'll get: issues, PRs, commits, releases, deployments.

To customize:

```bash
# Only get PR and review notifications
/github subscribe your-org/your-repo pulls reviews

# Stop getting issue notifications
/github unsubscribe your-org/your-repo issues

# See what you're currently subscribed to
/github subscribe list
```

**Recommended settings for a small team:**
```
/github subscribe your-org/your-repo pulls reviews comments
```

### Step 4 — Verify it works

Open a test PR or push a commit — you should see a message appear in the Slack channel within seconds.

---

## ⚠️ What NOT to Commit

- `.env` files with real passwords or API keys
- `node_modules/`, `venv/`, build output (`dist/`, `build/`)
- IDE configs (`.idea/`, `.vscode/`)
- Large binary files or test data dumps

When in doubt, check `.gitignore`.

---

## 💬 Team Rules

- If you're stuck for more than **30 minutes** → ask the team in Slack
- If you break `dev` → let everyone know **immediately**
- If you'll be inactive → give a heads-up in the group chat
- Review PRs **within 24 hours** so nobody is blocked

Small commits, clear messages, quick reviews = happy team. 🙌
