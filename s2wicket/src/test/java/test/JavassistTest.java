package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import junit.framework.TestCase;

public class JavassistTest extends TestCase {
	
	private static Class getProxyClass() throws Exception {
		ClassPool cp = ClassPool.getDefault();
		CtClass componentCC = cp.get("test.Component");
		CtClass proxyCC = cp.makeClass("Proxy", componentCC);
		CtClass serializableClazz = cp.get("java.io.Serializable");
		proxyCC.addInterface(serializableClazz);
		CtMethod onClickMethod = CtMethod.make(
				"public void onClick() {System.out.println(\"S2Wicket\");addHoge(\"bar\");}", proxyCC);
		proxyCC.addMethod(onClickMethod);
		CtMethod writeReplaceMethod = CtMethod.make(
				"public Object writeReplace() throws java.io.ObjectStreamException {"
			  + "    System.out.println(\"writeReplace() called.\");"
			  + "    java.util.Map fieldMap = new java.util.HashMap();"
			  + "    java.lang.reflect.Field[] fields = test.Component.class.getDeclaredFields();"
			  + "    for (int i = 0; i < fields.length; i++) {"
			  + "        java.lang.reflect.Field f = fields[i];"
			  + "        f.setAccessible(true);"
			  + "        Object value = f.get(this);"
			  + "        fieldMap.put(f.getName(), value);"
			  + "        System.out.println(f.getName() + \" : \" + value);"
			  + "    }"
			  + "    return new test.JavassistTest$SerializedProxy(fieldMap);"
			  + "}",
				proxyCC);
		proxyCC.addMethod(writeReplaceMethod);
		return proxyCC.toClass();
	}
	
	public void testJavassist() throws Exception {
//		Class clazz = getProxyClass();
//		Component proxy = (Component)clazz.newInstance();
//		proxy.onClick();
//		proxy.foo();
//		//
//		for (Field f : Component.class.getDeclaredFields()) {
//			f.setAccessible(true);
//			System.out.println(f.get(proxy));
//		}
//		//
//		FileOutputStream fos = new FileOutputStream("./test.ser");
//		ObjectOutputStream out = new ObjectOutputStream(fos);
//		out.writeObject(proxy);
//		out.close();
	}
	
	public static class SerializedProxy implements Serializable {
		
		private Map fieldMap;
		
		public SerializedProxy(Map fieldMap) {
			this.fieldMap = fieldMap;
		}
		
		public Object readResolve() throws ObjectStreamException {
			try {
				System.out.println("readResolve() called.");
				Class proxyClass = getProxyClass();
				Object component = proxyClass.newInstance();
				Set set = fieldMap.entrySet();
				for (Iterator i = set.iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					Field field = Component.class.getDeclaredField((String)entry.getKey());
					field.setAccessible(true);
					field.set(component, entry.getValue());
				}
				return component;
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		
	}
	
	public void testDeserialize() throws Exception {
//		FileInputStream fis = new FileInputStream("./test.ser");
//		ObjectInputStream in = new ObjectInputStream(fis);
//		Object obj = in.readObject();
//		in.close();
//		((Component)obj).onClick();
//		((Component)obj).foo();
	}
	
}
