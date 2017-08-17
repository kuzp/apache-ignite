/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.ignite.examples.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sbt-kuchevasov-vi
 */
public class Transform {
    
    public Object[] getObj (String txtline, Class clsData, Class clsKey) throws IllegalAccessException, ClassNotFoundException, Exception {
        return getObj(txtline, clsData, clsKey, null);
    }
    
    public Object[] getObj (String txtline, Class clsData, Class clsKey, HashMap<Long, String> clients) throws IllegalAccessException, ClassNotFoundException, Exception {
        return getObj(txtline, clsData, clsKey, clients, null);
    }    
    
    public Object[] getObj (String txtline, Class clsData, Class clsKey, HashMap<Long, String> clients, HashMap<String, HashMap<Long, Long>> dicts) throws InstantiationException, IllegalAccessException, ClassNotFoundException, Exception {
        Object obj = clsData.newInstance();        
        Object key;// = clsKey.newInstance();        
        txtline = "#|"+txtline+"|#";
        String[] txts = txtline.split("\\|");
        long partId = 0l;
        long id = 0l;
        long rootId = 0l;
        Field paritionField = null;
//        
//        if (clients != null) {
//            if (!clients.containsKey((long)fld_value)) {
////                            Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, clients.size()+"  "+(long)fld_value);
//                throw new Exception("клиент не найден в списке");
//            } else {
//                partId = Long.valueOf(clients.get(fld_value).split("_")[0]);
//                rootId = Long.valueOf(clients.get(fld_value).split("_")[1]);
//            }
//
//        }

        for (Field field:obj.getClass().getFields()) {
            TransformType tt = TransformType.NONE;
            int ino = 0;
            String idfield = "";
            String partfield = "";
            String rootfield = "";
            String partforfield = "";
            String def = "";
            Boolean isClient = false;
            String dictName = null;

            for (Annotation ant:field.getAnnotations()) {

                if (ant instanceof DataType) {
                    DataType dt = field.getAnnotation(DataType.class);
                    tt = dt.value();
//                    System.err.println(tt);
                } else if (ant instanceof ClientField) {
                    isClient = true;
                } else if (ant instanceof InitOrder) {
                    InitOrder inorder = field.getAnnotation(InitOrder.class);
                    ino = Integer.valueOf(inorder.value());
//                    System.err.println(ino);
                } else if (ant instanceof IdField) {
                    IdField idf = field.getAnnotation(IdField.class);
                    idfield = idf.value();
//                    System.err.println(idfield);
                } else if (ant instanceof PartField) {
                    PartField pf = field.getAnnotation(PartField.class);
                    partfield = pf.value();
                    paritionField = field;
//                    System.err.println(partfield);
                } else if (ant instanceof RootField) {
                    RootField rf = field.getAnnotation(RootField.class);
                    rootfield = rf.value();
//                    System.err.println(partfield);
                } else if (ant instanceof PartForField) {
                    PartForField pff = field.getAnnotation(PartForField.class);
                    partforfield = pff.value();
//                    System.err.println(partforfield);
                } else if (ant instanceof Default) {
                    Default dflt = field.getAnnotation(Default.class);
                    def = dflt.value();
//                    System.err.println(def);
                } else if (ant instanceof DictName) {
                    DictName dict = field.getAnnotation(DictName.class);
                    dictName = dict.value();
                }
//                System.err.println(ant);

            }


//            PartForField pff = field.getAnnotation(PartForField.class);
//            Annotation[] ants = field.getAnnotations();
//            if (ants.length > 0) {
                Object fld_value = null;
                if (ino > 0) {
                    String txt = txts[ino];
                    if (tt.name().contains("CSL_PARTICLES")) {
                        fld_value = tt.getValue(txt,partId);
                    } else if (tt.name().contains("CSL_AFFINITYPARTICLES"))     {
                        fld_value = tt.getValue(txt,partId,rootId);
                    } else if (tt.name().contains("OBJ_TYPE") || tt.name().contains("OBJ_BIGDECIMAL")) {
                        fld_value = tt.getValue(txt,txts[ino+1]);
                    } else {
                        fld_value = tt.getValue(txt);
                    }

                } else {
                    if (tt.name().contains("PARTITION")) {
                        Long pff_value = null;
                        try {
                            Field fld_id = obj.getClass().getDeclaredField(partforfield) ;
                            fld_id.setAccessible(true);
                            pff_value = fld_id.getLong(obj);
                        } catch (NoSuchFieldException ex) {
                            Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SecurityException ex) {
                            Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (pff_value == null || pff_value == 0) {
                            fld_value = 0l;
                        } else {
                            fld_value = partId;
                        }

                    }
                    if (tt.name().contains("ROOT")) {
                        Field fld_id = null;
                        Long rff_value = null;
                            try {
                                fld_id = obj.getClass().getDeclaredField(partforfield) ;
                                if (fld_id != null ) {
                                    fld_id.setAccessible(true);
                                    rff_value = fld_id.getLong(obj);
                                }
                            } catch (NoSuchFieldException ex) {
//                                Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SecurityException ex) {
                                Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        if ((rff_value == null || rff_value == 0) && fld_id != null)  {
                            fld_value = 0l;
                        } else {
                            fld_value = rootId;
                        }
                    }

                }

                if (partfield.equals("true")) {
                    fld_value = (fld_value == null || (Long)fld_value == 0l) ? -1l : fld_value;
                    partId = (long) fld_value;
                }

                if (idfield.equals("true")) {
                    id = (long) fld_value;
                }

                if (rootfield.equals("true")) {
                    if (clients != null) {
                        if (!clients.containsKey((long)fld_value)) {
                            if (clients.containsKey(-1l)) {
                                partId = Long.valueOf(clients.get(-1l).split("_")[0]);
                                rootId = Long.valueOf(clients.get(-1l).split("_")[1]);
                            } else {
                                throw new Exception("клиент " + fld_value + " не найден в списке");
                            }
                        } else {
                            partId = Long.valueOf(clients.get(fld_value).split("_")[0]);
                            rootId = Long.valueOf(clients.get(fld_value).split("_")[1]);
                        }
                    } else rootId = (long) fld_value;
                }

                if ((isClient || rootfield.equals("true")) & clients != null) {
                    if (rootId != 0) {
                        fld_value = rootId;
                    } else {
                        if (clients.containsKey((long)fld_value)) {
                            fld_value = Long.valueOf(clients.get(fld_value).split("_")[1]);
                        } else {
                            if (clients.containsKey(-1l)) {
                                partId = Long.valueOf(clients.get(-1l).split("_")[0]);
                                rootId = Long.valueOf(clients.get(-1l).split("_")[1]);
                            } else {
                                throw new Exception("клиент " + fld_value + " не найден в списке");
                            }
                        }
                    }
                }

                if (fld_value != null && dictName != null && dicts != null) {
                    if (dicts.containsKey(dictName)) {
                        if (dicts.get(dictName).containsKey(fld_value)) {
                            fld_value = dicts.get(dictName).get(fld_value);
                        } else {
                            throw new Exception("В справочнике "+dictName+" не надено значение "+fld_value);
                        }
                    } else {
                        throw new Exception("Ошибка модели, нет такого справочника");
                    }
                }

                if (fld_value == null && !def.isEmpty()) {
                    try {
                        fld_value = tt.getValue(def);
                    } catch (Exception e) {
                        throw new Exception(e.getMessage()+" поле "+field.getName());
                    }
                }
                
                if (fld_value!=null) {
                    field.set(obj, fld_value);
                }
//                 System.err.println(field.getName()+" "+fld_value);
//            } 
        }
        
        if (paritionField != null) paritionField.set(obj, partId);
        
        key = getKey(clsKey, id, partId, rootId);
        Object[] out = {key,obj,id};

        return out;
    }
    
    private Object getKey (Class clsKey, long id, long partId, long rootId) {
                                Object key = null;        
//                                    			doo.setId(doo.getFields().get(name));
        //                            Object key = new com.sbt.dpl.gridgain.ParticleKey(1);
//                                    Class<?> cls = Class.forName("com.sbt.dpl.gridgain.ParticleKey");
//                                    if (!cls.isPrimitive()) {
                                    
                                    Constructor<?>[] cc = clsKey.getConstructors();
                                    
                                    for (Constructor<?> c: cc) {
                                        Class[] cltype = c.getParameterTypes();
//                                        System.err.println(clsKey.getName()+" "+c.getParameterCount());                                        
                                        if (clsKey.getName().equals("com.sbt.dpl.gridgain.AffinityParticleKey") && c.getParameterTypes().length != 3) {
                                            
                                            continue;
                                        }

                                        if (c.getParameterTypes().length== 2) {
//                                        if (cltype.length == 2) {    
                                            try {
//                                                                                    this.keyConstructor = c;
//                                            if (CheckClass.isPrimitive(c.getParameterTypes()[0]) && CheckClass.isPrimitive(c.getParameterTypes()[0])) break;
                                                key = c.newInstance(new Object[] {id, partId});
                                            } catch (InstantiationException ex) {
                                                Logger.getLogger(TransformType.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (IllegalAccessException ex) {
                                                Logger.getLogger(TransformType.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (IllegalArgumentException ex) {
                                                Logger.getLogger(TransformType.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (InvocationTargetException ex) {
                                                Logger.getLogger(TransformType.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                        } else if (c.getParameterTypes().length == 3) {
//                                        } else if (cltype.length == 2) {    
                                            
                                            try {
                                                key = c.newInstance(new Object[] {id, partId, rootId});
                                            } catch (InstantiationException ex) {
                                                Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (IllegalAccessException ex) {
                                                Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (IllegalArgumentException ex) {
                                                Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (InvocationTargetException ex) {
                                                Logger.getLogger(Transform.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        } else {
                                            key=id;
                                        }
                                        break;
                                    }
      
        return key;
    }
}
