module SecurityServiceModule {
    requires ImageServiceModule;
    requires java.desktop;
    requires miglayout.swing;
    requires com.google.common;
    requires java.prefs;
    requires gson;
    opens com.udacity.security.data to gson;
}