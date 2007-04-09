package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

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
//		enhancer.setInterfaces(new Class[] {Serializable.class/*, WriteReplaceHolder.class*/});
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
		enhancer.setInterfaces(new Class[] {Serializable.class/*, WriteReplaceHolder.class*/});
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
//			} else if (methodName.equals("writeReplace")) {
//				System.out.println("ComponentMethodInterceptor#intercept()#writeReplace()");
//				return writeReplace(obj);
			} else {
				return proxy.invokeSuper(obj, args);
			}
		}
		
//		public Object writeReplace(Object obj) throws ObjectStreamException {
//			System.out.println("ComponentMethodInterceptor#writeReplace()");
//			System.out.println("this = " + getClass().getName());
//			System.out.println("obj = " + obj);
//			return new SerializedProxy((Component)obj);
//		}
		
	}
	
//	private static class SerializedProxy implements Serializable {
//		
//		static {
//			System.out.println("SerializedProxy#static{}");
//		}
//		
//		private Object target;
//		
//		public SerializedProxy(Object target) {
//			this.target = target;
//		}
//		
//		public Object readResolve() throws ObjectStreamException {
//			System.out.println("readResolve() = " + target);
//			return target;
//		}
//		
//	}
	
	public static abstract class Component {
		
		public abstract void doClick();
		
	}
	
//	public static interface WriteReplaceHolder {
//		
//		public Object writeReplace() throws ObjectStreamException;
//		
//	}

}
