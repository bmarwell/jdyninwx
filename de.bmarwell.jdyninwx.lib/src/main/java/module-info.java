module jdyninwx.lib {
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires jdyninwx.xml;
    requires de.bmarwell.jdyninwx.common.value;

    exports de.bmarwell.jdyninwx.lib.services;
}
