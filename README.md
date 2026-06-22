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

## 🛠️ GitHub Integration Setup (How to set it up!)

To use the **in-app custom path submission tool** and send data directly from LuxKey to your repository server:

1.  **Create a GitHub Repository:**
    *   Log in to Github and create a repository (e.g., `yourusername/luxkey-paths` or keep it private/public).
2.  **Generate a Personal Access Token (PAT):**
    *   Navigate to **Settings > Developer Settings > Personal Access Tokens > Tokens (classic)**.
    *   Click **Generate new token**.
    *   Select the **`repo`** scope (or **`public_repo`** scope if your repository is public). This is required to write issues.
    *   Copy the generated token string safely.
3.  **Run the Submission Dialog in LuxKey:**
    *   Tap the **Cloud Upload icon (FAB)** in the bottom right corner (placed nicely above the refresh button).
    *   Pasted your **Personal Access Token** and enter your **Repository Path** (`owner/repo` format, such as `yourusername/luxkey-paths`).
    *   Tap **Odeslat**. An issue detailing your brand, phone model, active/inactive values, and verified sysfs paths will be uploaded automatically!

---

## 💾 Core Specifications

*   **Min SDK:** 26 (Android 8.0 Oreo)
*   **Root requirements:** Standard SuperUser (`su`) access for sysfs manipulation.
*   **UI Framework:** Jetpack Compose (Material Design 3 with dynamic dark/light surface pairing)
*   **Database Engine:** Room persistence engine for custom values caching and history.

---

## 📝 License & Attribution

Formulated and developed with passion by **antunlar**. Fully optimized with modern Android architectural components.
