# LuxKey 💡

LuxKey is a premium, high-performance Android utility designed for advanced customization of tactile physical button backlights, keyboard LEDs, and touchkey controllers on custom ROMs (such as HavocOS, LineageOS, PixelExperience) or rooted stock firmwares.

Made with precision and material aesthetics by **antunlar**.

---

## 🚀 Key Features

*   **🔒 Safety Shell Write Lock:** Automatically protects your system nodes from accidental, corrupted, or erroneous writing. Writing capabilities are unlocked only after user customization or when explicitly toggled.
*   **🔍 Active HW Status Sensing:** Instantly interrogates the target hardware backlight on launch to verify its active power state BEFORE rendering active toggle choices.
*   **📂 Preloaded Verified ROM Nodes:** Features symmetric touchkey controllers like the **Samsung Galaxy A3 2017 with HavocOS** out-of-the-box (`/sys/devices/virtual/sec/sec_touchkey/brightness`).
*   **☁️ Direct In-App GitHub Synchronization:** Instantly upload and propose newly discovered verified backlight nodes directly to any selected GitHub repository as structured issues.
*   **📱 Size-Responsive Launcher Widget:** Supports a sleek homescreen shortcut widget that automatically simplifies its visual content to fit compact, standard, or expanded widget sizes.
*   **🪵 Root Command Action History:** Complete session logging of executed commands, permissions changes (`chmod`), and standard system outputs.

---

## 🛠️ GitHub Integration Setup

The custom path submission service is **fully automated** out-of-the-box:

1.  **Configure Environment Variables (Optional):**
    *   To customize the target repository or use your own Personal Access Token (PAT), define `GITHUB_PAT` and `GITHUB_REPO` in the **Secrets panel in AI Studio** (which automatically maps to `.env`).
2.  **Zero-configuration Fallback:**
    *   By default, the application is pre-configured with the secure database repo **`antunlargg/luxkey-db`** and corresponding credentials.
3.  **Submit Backlight Profile:**
    *   Tap the **Cloud Upload icon (FAB)** in the bottom right corner (placed beautifully above the sync refresh button).
    *   The dialog automatically displays your detected phone brand, model, customized path, and verified active/inactive values.
    *   Add any custom remarks in **Poznámky k zařízení** and click **Odeslat**. The backlight node will be published immediately to the remote LuxKey database! All credentials remain perfectly hidden from the user interface.

---

## 💾 Core Specifications

*   **Min SDK:** 26 (Android 8.0 Oreo)
*   **Root requirements:** Standard SuperUser (`su`) access for sysfs manipulation.
*   **UI Framework:** Jetpack Compose (Material Design 3 with dynamic dark/light surface pairing)
*   **Database Engine:** Room persistence engine for custom values caching and history.

---

## 📝 License & Attribution

Formulated and developed with passion by **antunlar**. Fully optimized with modern Android architectural components.
