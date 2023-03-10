module jdyninwx.app {
    requires info.picocli;
    requires jdyninwx.lib;
    requires org.slf4j;

    opens de.bmarwell.jdyninwx.app to
            info.picocli;
    opens de.bmarwell.jdyninwx.app.commands to
            info.picocli;

    exports de.bmarwell.jdyninwx.app;
    exports de.bmarwell.jdyninwx.app.settings;
}
