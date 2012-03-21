package org.seasar.wicket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.settings.IApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadingJavaSerializer extends JavaSerializer {
    private static final Logger log =
            LoggerFactory.getLogger(ReloadingJavaSerializer.class);

    public ReloadingJavaSerializer(String applicationKey) {
        super(applicationKey);
    }

    @Override
    protected ObjectInputStream newObjectInputStream(InputStream in)
            throws IOException {
        return new ClassResolverObjectInputStream(in);
    }

    private static class ClassResolverObjectInputStream extends
            ObjectInputStream {
        public ClassResolverObjectInputStream(InputStream in)
                throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc)
                throws IOException, ClassNotFoundException {
            String className = desc.getName();

            try {
                return Thread.currentThread().getContextClassLoader().loadClass(
                        desc.getName());
            } catch (ClassNotFoundException ex1) {
                log.debug("Class not found by the object outputstream itself, trying the IClassResolver");
            }

            Class<?> candidate = null;
            try {
                // Can the application always be taken??
                // Should be if serialization happened in thread with application set
                // (WICKET-2195)
                Application application = Application.get();
                IApplicationSettings applicationSettings =
                        application.getApplicationSettings();
                IClassResolver classResolver =
                        applicationSettings.getClassResolver();

                candidate = classResolver.resolveClass(className);
                if (candidate == null) {
                    candidate = super.resolveClass(desc);
                }
            } catch (WicketRuntimeException ex) {
                if (ex.getCause() instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) ex.getCause();
                }
            }
            return candidate;
        }
    }
}
