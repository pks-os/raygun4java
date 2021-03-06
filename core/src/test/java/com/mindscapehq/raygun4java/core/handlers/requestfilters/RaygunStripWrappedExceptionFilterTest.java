package com.mindscapehq.raygun4java.core.handlers.requestfilters;

import com.mindscapehq.raygun4java.core.messages.RaygunErrorMessage;
import com.mindscapehq.raygun4java.core.messages.RaygunMessage;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RaygunStripWrappedExceptionFilterTest {

    @Test
    public void shouldStripException () {
        RaygunStripWrappedExceptionFilter f = new RaygunStripWrappedExceptionFilter(ClassNotFoundException.class);

        RaygunMessage message = new RaygunMessage();
        message.getDetails().setError(new RaygunErrorMessage(new ClassNotFoundException("wrapper", new Exception("keep me!"))));
        f.onBeforeSend(null, message);

        assertThat(message.getDetails().getError().getMessage(), is("Exception: keep me!"));
    }

    @Test
    public void shouldNotStripException() {
        RaygunStripWrappedExceptionFilter f = new RaygunStripWrappedExceptionFilter(NullPointerException.class);

        RaygunMessage message = new RaygunMessage();
        message.getDetails().setError(new RaygunErrorMessage(new ClassNotFoundException("wrapper", new Exception("keep me!"))));
        f.onBeforeSend(null, message);

        assertThat(message.getDetails().getError().getMessage(), is("ClassNotFoundException: wrapper"));
    }

    @Test
    public void shouldNotStripExceptionIfNotFirst() {
        RaygunStripWrappedExceptionFilter f = new RaygunStripWrappedExceptionFilter(ClassNotFoundException.class);

        RaygunMessage message = new RaygunMessage();
        message.getDetails().setError(new RaygunErrorMessage(new Exception("keep me!", new ClassNotFoundException("keep me too!"))));
        f.onBeforeSend(null, message);

        assertThat(message.getDetails().getError().getMessage(), is("Exception: keep me!"));
    }

    @Test
    public void shouldStripNestedExceptions () {
        RaygunStripWrappedExceptionFilter f = new RaygunStripWrappedExceptionFilter(ClassNotFoundException.class);

        RaygunMessage message = new RaygunMessage();
        message.getDetails().setError(new RaygunErrorMessage(new ClassNotFoundException("wrapper1", new ClassNotFoundException("wrapper2", new Exception("keep me!")))));
        f.onBeforeSend(null, message);

        assertThat(message.getDetails().getError().getMessage(), is("Exception: keep me!"));
    }

    @Test
    public void shouldLeaveOneStrippedNestedException () {
        RaygunStripWrappedExceptionFilter f = new RaygunStripWrappedExceptionFilter(ClassNotFoundException.class, IllegalStateException.class);

        RaygunMessage message = new RaygunMessage();
        message.getDetails().setError(new RaygunErrorMessage(new ClassNotFoundException("wrapper1", new ClassNotFoundException("wrapper2", new IllegalStateException("wrapper3")))));
        f.onBeforeSend(null, message);

        assertThat(message.getDetails().getError().getMessage(), is("IllegalStateException: wrapper3"));
    }

    @Test
    public void shouldStripMultipleNestedException () {
        RaygunStripWrappedExceptionFilter f = new RaygunStripWrappedExceptionFilter(ClassNotFoundException.class, IllegalStateException.class);

        RaygunMessage message = new RaygunMessage();
        message.getDetails().setError(new RaygunErrorMessage(new ClassNotFoundException("wrapper1", new IllegalStateException("wrapper2", new Exception("keep me!")))));
        f.onBeforeSend(null, message);

        assertThat(message.getDetails().getError().getMessage(), is("Exception: keep me!"));
    }

    @Test
    public void shouldReturnSameInstanceFromCreateFactoryFunction() {
        RaygunStripWrappedExceptionFilter factory = new RaygunStripWrappedExceptionFilter(ClassNotFoundException.class);
        assertThat(factory.create(), is(factory.create()));
    }

}