module jdyninwx.app {
    requires jdyninwx.xml;
    requires jdyninwx.lib;
    requires info.picocli;
    requires org.slf4j;

    opens de.bmarwell.jdyninwx.app to
            info.picocli;
    opens de.bmarwell.jdyninwx.app.commands to
            info.picocli;

    exports de.bmarwell.jdyninwx.app;
    exports de.bmarwell.jdyninwx.app.settings;
}
