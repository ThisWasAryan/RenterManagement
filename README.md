# RMS Landing Page

GitHub Pages website for [Rent Management System](https://github.com/ThisWasAryan/RenterManagement).

## Structure

```
rms-landing/
├── index.html                      ← Main page
├── .nojekyll                       ← Disables Jekyll on GitHub Pages
└── assets/
    ├── style.css                   ← All styles
    ├── script.js                   ← Scroll reveal, nav, mobile menu
    └── screenshots/
        ├── README.md               ← Screenshot naming guide
        ├── screen-dashboard.png    ← Home dashboard (hero + walkthrough §1)
        ├── screen-tenants.png      ← Tenants screen (hero back phone)
        ├── screen-payments.png     ← Payments/ledger (walkthrough §2)
        ├── screen-documents.png    ← Document cabinet (walkthrough §3)
        └── screen-settings.png    ← Settings screen (walkthrough §4)
```

## Deploying to GitHub Pages

1. Create a new repository (e.g. `rms-landing`) or use your existing `RenterManagement` repo.
2. Push all files in this folder to the `main` (or `gh-pages`) branch.
3. In the repository settings → Pages → set source to `main` branch, `/ (root)` folder.
4. Add your screenshots to `assets/screenshots/` following the naming scheme in `assets/screenshots/README.md`.

The site works immediately even without screenshots — placeholder boxes display until images are added.

## Screenshot Names

| File                       | Screen                |
|----------------------------|-----------------------|
| `screen-dashboard.png`     | Home Dashboard        |
| `screen-tenants.png`       | Tenants list/detail   |
| `screen-payments.png`      | Payments & Ledger     |
| `screen-documents.png`     | Document Cabinet      |
| `screen-settings.png`      | Settings              |

All screenshots should be portrait 9:16 (e.g. 1080×1920).
