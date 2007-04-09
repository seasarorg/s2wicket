package test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CGLibTest extends TestCase {
	
	public void testSerialize() throws Exception {
//		Enhancer enhancer = new Enhancer();
//		enhancer.setSuperclass(Component.class);
//		enhancer.setInterfaces(new Class[] {Serializable.class, WriteReplaceHolder.class});
//		enhancer.setCallback(new ComponentMethodInterceptor());
//		enhancer.setNamingPolicy(new NamingPolicy() {
//			public String getClassName(String prefix, String source, Object key, Predicate names) {
//				String className = "test.Hoge$" + source.substring(source.lastIndexOf('.') + 1);
//				String k = key.toString();
//				int i = k.indexOf(',');
//				if (i != -1) { 
//					className += "$" + k.substring(k.indexOf('$') + 1, k.indexOf(','));
//				} else {
//					className += "$" + k.substring(k.indexOf('$') + 1);
//				}
//				System.out.println("className = [" + className + "]");
//				return className;
//			}
//		});
//		Component proxy = (Component)enhancer.create();
//		System.out.println("proxy = " + proxy.getClass().getName());
//		proxy.doClick();
//		FileOutputStream fos = new FileOutputStream("./test.ser");
//		ObjectOutputStream out = new ObjectOutputStream(fos);
//		out.writeObject(proxy);
//		out.close();
	}
	
	public void testDeserialize() throws Exception {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(Component.class);
		enhancer.setInterfaces(new Class[] {Serializable.class, WriteReplaceHolder.class});
		enhancer.setCallback(new ComponentMethodInterceptor());
		enhancer.setNamingPolicy(new NamingPolicy() {
			public String getClassName(String prefix, String source, Object key, Predicate names) {
				String className = "test.Hoge$" + source.substring(source.lastIndexOf('.') + 1);
				String k = key.toString();
				int i = k.indexOf(',');
				if (i != -1) { 
					className += "$" + k.substring(k.indexOf('$') + 1, k.indexOf(','));
				} else {
					className += "$" + k.substring(k.indexOf('$') + 1);
				}
				System.out.println("className = [" + className + "]");
				return className;
			}
		});
		Component proxy = (Component)enhancer.create();
		//
		FileInputStream fis = new FileInputStream("./test.ser");
		ObjectInputStream in = new ObjectInputStream(fis);
		Object obj = in.readObject();
		in.close();
		((Component)obj).doClick();
	}
	
	private static class ComponentMethodInterceptor
			implements MethodInterceptor, Serializable {
		
		private int i = 0;

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			String methodName = method.getName();
			System.out.println("methodName = " + methodName);
			if (methodName.equals("doClick")) {
				System.out.println("doClick!" + i++);
				return null;
			} else if (methodName.equals("writeReplace")) {
				System.out.println("ComponentMethodInterceptor#intercept()#writeReplace()");
				return writeReplace0(obj);
			} else {
				return proxy.invokeSuper(obj, args);
			}
		}
		
		public Object writeReplace0(Object obj) throws ObjectStreamException {
			System.out.println("writeReplace0");
			try {
				Class<? extends Object> clazz = obj.getClass();
				Map<String, Object> fieldMap = new HashMap<String, Object>();
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					int modifiers = field.getModifiers();
					if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
						fieldMap.put(field.getName(), field.get(obj));
					}
				}
				return new SerializedProxy(clazz, fieldMap);
			} catch(IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
	}
	
	private static class SerializedProxy implements Serializable {
		
		private Class<? extends Object> superClazz;
		
		private Map<String, Object> fields;
		
		public SerializedProxy(Class<? extends Object> superClazz, Map<String, Object> fields) {
			this.superClazz = superClazz;
			this.fields = fields;
		}
		
		public Object readResolve() throws ObjectStreamException {
			try {
				Enhancer enhancer = new Enhancer();
				enhancer.setSuperclass(superClazz);
				enhancer.setInterfaces(new Class[] {Serializable.class, WriteReplaceHolder.class});
				enhancer.setCallback(new ComponentMethodInterceptor());
				enhancer.setNamingPolicy(new NamingPolicy() {
					public String getClassName(String prefix, String source, Object key, Predicate names) {
						String className = "test.Hoge$" + source.substring(source.lastIndexOf('.') + 1);
						String k = key.toString();
						int i = k.indexOf(',');
						if (i != -1) { 
							className += "$" + k.substring(k.indexOf('$') + 1, k.indexOf(','));
						} else {
							className += "$" + k.substring(k.indexOf('$') + 1);
						}
						System.out.println("className = [" + className + "]");
						return className;
					}
				});
				Object proxy = enhancer.create();
				for (String key : fields.keySet()) {
					Field field = superClazz.getDeclaredField(key);
					field.set(proxy, fields.get(key));
				}
				return proxy;
			} catch(NoSuchFieldException e) {
				throw new IllegalStateException(e);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		
	}
	
	public static abstract class Component {
		
		public abstract void doClick();
		
	}
	
	public static interface WriteReplaceHolder {
		
		public Object writeReplace() throws ObjectStreamException;
		
	}

}
