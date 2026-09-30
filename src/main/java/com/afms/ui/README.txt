FOLDER: ui/   Package: com.afms.ui

FILES TO CREATE
[39] LoginFrame.java
     Split window: brand panel + email/password form + demo account buttons.
     Calls AuthService.login, then opens MainFrame.
[40] MainFrame.java
     App shell: dark sidebar built from user.getMenu(), user card with
     sign-out, content area. Calls Pages.build(user, menuName, navigator).

SUB-PACKAGES
kit/    [29]-[38]  reusable components
pages/  [41]-[47]  role screens
