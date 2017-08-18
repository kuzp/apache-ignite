package org.apache.ignite.examples.loader;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;



public enum TransformType {
	STRING {
		@Override
                public Object getValue(String value) {
                    return value;
                }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }


	},
        PARTITION {
                  @Override
                  public Object getValue(String value) {
                      long partId=0l;
                      return partId;
                  }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        },
        ROOT {
                  @Override
                  public Object getValue(String value) {
                      long rootId=0l;
                      return rootId;
                  }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        },
        INTEGER{
		@Override
                public Object getValue(String value) {
			String s =  value;
			try {
				return s.length()>0?Integer.valueOf(s):null;
			} catch (Exception e) {
				throw new IllegalArgumentException("������ "+s+" �� ���������� ��� �����(int)");
			}
		}

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	},
	LONG{
            @Override
             public Object getValue(String value) {
                    String s =  value;
//                    if (s.length()>20) s = s.substring(0, 20);//workaround
                    Long l = null;
//                        System.out.print(name+" "+s+"\n");
                    try {
                        if (s.length()>0) {
                            if (s.contains(".")) {
                                BigDecimal bd = new BigDecimal(s);
                                l = bd.longValue();
                                if (l%1 != 0) {
                                    throw new IllegalArgumentException("������ "+s+" �� ���������� ��� �����(long)");
                                }
                            } else l=Long.valueOf(s);
                        }
//                            System.out.print(name+" "+l+"\n");
                        return l;
                    } catch (Exception e) {
                            throw new IllegalArgumentException("������ "+s+" �� ���������� ��� �����(long)");
                    }
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	},
//	DATE{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {
//			String s =  scanner.next();
//			try {
//				doo.setField(name,convertToDate(s));
//			} catch (Exception e) {
//				throw new IllegalArgumentException("������ " + s
//						+ " �� ���������� ��� ���� :"
//						+ e.getMessage());
//			}
//		}
//	},
//	DATE_01011900IFNULL{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {
//			String s =  scanner.next();
//			try {
//				doo.setField(name,convertToDate(s,default_01011900));
//			} catch (Exception e) {
//				throw new IllegalArgumentException("������ " + s
//						+ " �� ���������� ��� ���� :"
//						+ e.getMessage());
//			}
//		}
//	},
	DATE_TIME {
            @Override
            public Object getValue(String value) {
                    try {
//                        Date dt = convertToDate(value);

//                        formatTime1.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

                        if (value.equals("") || value == null)
                            return null;

                        SimpleDateFormat formatTime1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",Locale.ROOT);

                        Date dt = formatTime1.parse(value);

                        if (dt.getYear() + 1900 < 1970) // filter illegal dates
                            return null;

                        return	dt;

                    } catch (Exception e) {
                            throw new IllegalArgumentException("������ " + value
                                            + " �� ���������� ��� ���� � �����:"
                                            + e.getMessage());
                    }
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	},
	DOUBLE{
		@Override
		public Object getValue(String value) {
                        Double d = null;
			try {
				d = (value.length()>0)?Double.valueOf(value):null;
                                return d;
			} catch (Exception e) {
				throw new IllegalArgumentException("������ "+value+" �� ���������� ��� ������� �����");
			}
		}

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	},
//        TRUE{
//            @Override
//            protected  void setupElement(Scanner scanner,String name,DataObject doo) {
//                doo.setField(name,true);
//            }
//
//        },
//        FALSE{
//            @Override
//            protected  void setupElement(Scanner scanner,String name,DataObject doo) {
//                doo.setField(name,false);
//            }
//
//        },
	NONE{
            @Override
            public Object getValue(String value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	},
//	ID_OPT{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {
//			if(scanner.hasNext()){
//			String id = scanner.next();
//			while(scanner.hasNext()&&!id.endsWith("]")){
//				id+="|"+scanner.next();
//				id = id.trim();
//			}
//			if(!id.contains("=")||!id.endsWith("]")||!id.contains("["))
//				throw new IllegalStateException("ID �� ���������:"+id);
//			name = id.substring(0,id.lastIndexOf("="));
//			id = id.substring(id.lastIndexOf("=")+2,id.length()-1);
//			id = id.replaceAll("\\.", "-");
//			doo.setField(name, id);
//			}
//		}
//	},
//        FIELD_AS_PARTID{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {
//                    if(doo.getFields().containsKey(name))	{
//                        doo.setPartId(doo.getFields().get(name));
//                    }
//                }
//        },
//	FIELD_AS_ID{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {
//			if(doo.getFields().containsKey(name))	{
//                                        doo.setId(doo.getFields().get(name));
//                        }
//		}
//	},
//	CSL_LONGS{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {
//			if(scanner.hasNext()){
//				String id = scanner.next();
////                                id=id.replace(",,", ",");
//				Scanner sc = new Scanner(id);
//				sc.useDelimiter(",");
//				Collection<Long> idsCol = new ArrayList<>();
//				while(sc.hasNext()){
//					if(sc.hasNextLong()){
//						idsCol.add(sc.nextLong());
//					}else{
//						throw new IllegalArgumentException("������ ��������������  "+id+" � ������ �� Long");
//					}
//				}
//				doo.setField(name, idsCol);
//			}
//		}
//	},
	CSL_DICTS{
		@Override
		public Object getValue(String value) {
				String id = value;
//                                id=id.replace(",,", ",");
				Scanner sc = new Scanner(id);
				sc.useDelimiter(",");
				Collection<Long> idsCol = new ArrayList<>();
				while(sc.hasNext()){
					if(sc.hasNextLong()){
						idsCol.add(sc.nextLong());
					}else{
						throw new IllegalArgumentException("������ ��������������  "+id+" � ������ �� Long");
					}
				}
                                StringBuilder sb = new StringBuilder();
                                for (Long cid : idsCol) {
                                    char[] compactKey = new char[8];
                                    System.arraycopy(compact(cid), 0, compactKey, 0, 4);
                                    sb.append(compactKey);
                                }
				return sb;
		}

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	},
	CSL_PARTICLES{
		@Override
		public Object getValue(String value, long partId) {
				String id = value;
//                                id=id.replace(",,", ",");
				Scanner sc = new Scanner(id);
				sc.useDelimiter(",");
				Collection<Long> idsCol = new ArrayList<>();
				while(sc.hasNext()){
					if(sc.hasNextLong()){
						idsCol.add(sc.nextLong());
					}else{
						throw new IllegalArgumentException("������ ��������������  "+id+" � ������ �� Long");
					}
				}
                                StringBuilder sb = new StringBuilder();
                                for (Long cid : idsCol) {
                                    char[] compactKey = new char[8];
                                    System.arraycopy(compact(cid), 0, compactKey, 0, 4);
                                    System.arraycopy(compact(partId), 0, compactKey, 4, 4);
                                    sb.append(compactKey);
                                }

				return sb;
		}

            @Override
            public Object getValue(String value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	},

	CSL_AFFINITYPARTICLES{
		@Override
		public Object getValue(String value, long partId, long rootId) {
				String id = value;
//                                id=id.replace(",,", ",");
				Scanner sc = new Scanner(id);
				sc.useDelimiter(",");
				Collection<Long> idsCol = new ArrayList<>();
				while(sc.hasNext()){
					if(sc.hasNextLong()){
						idsCol.add(sc.nextLong());
					}else{
						throw new IllegalArgumentException("������ ��������������  "+id+" � ������ �� Long");
					}
				}
                                StringBuilder sb = new StringBuilder();
                                for (Long cid : idsCol) {
                                    char[] compactKey = new char[12];
                                    System.arraycopy(compact(cid), 0, compactKey, 0, 4);
                                    System.arraycopy(compact(partId), 0, compactKey, 4, 4);
                                    System.arraycopy(compact(rootId), 0, compactKey, 8, 4);
                                    sb.append(compactKey);
                                }

				return sb;
		}

            @Override
            public Object getValue(String value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

	},
        PARAM_TYPE{

            @Override
            public Object getValue(String value) {

                switch (value) {
                    case "D": return -9223372036854775788l;
                    case "I": return -9223372036854775787l;
                    case "F": return -9223372036854775786l;
                    default: return -9223372036854775785l;
                }
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        },
	OBJ_TYPE{
            @Override
            public Object getValue(String value, String type) {
//			if(scanner.hasNext()){

//				String value = scanner.next();
        				String valType  = type;
					switch (valType) {
						case "int":
						case "INT":
							try {
                                                            long l = value.length()>0?Long.parseLong(value):null;
                                                            value = String.valueOf(l);
                                                                return value;
//								doo.setField(name, value);
							} catch (Exception e) {
								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Integer:"+e.getMessage());
							}
						case "d":
						case "D":
							try {
                                                            return convertDateToString(value);
//								doo.setField(name,convertToDate(value).toGMTString());
							} catch (Exception e) {
								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Date:"+e.getMessage());
							}
						case "f":
						case "F":
							try {
                                                            double d = value.length()>0?Double.parseDouble(value):null;
                                                            value = String.valueOf(d);
                                                            return value;
//								doo.setField(name, value);
							} catch (Exception e) {
								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Double:"+e.getMessage());
							}

						case "s":
						case "S":
							try {
                                                            return value;
//								doo.setField(name, value);
							} catch (Exception e) {
								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� String:"+e.getMessage());
							}

						case "i":
						case "I":
						case "l":
						case "L":
							try {
                                                            Long l = value.length()>0?Long.parseLong(value):null;
                                                            value = String.valueOf(l);
                                                            return value;
//								doo.setField(name, value);
							} catch (Exception e) {
								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Long:"+e.getMessage());
							}
//							break;

						default:
							throw new IllegalArgumentException("�������� "+value+" �������� ����������� ���:"+valType);
						}


//					}else{
//						throw new IllegalArgumentException("�������� �� �������� ���, ����� ����������� \"|\"");
//					}
//				}else{
//					doo.setField(name, null);
//                                        scanner.next();
////                                        throw new IllegalArgumentException("�������� ������");
//				}
//			}
		}

            @Override
            public Object getValue(String value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        },
	OBJ_BIGDECIMAL{
            @Override
            public Object getValue(String value, String type) {
//			if(scanner.hasNext()){

//				String value = scanner.next();
				if(value.trim().length()>0){
//					if(scanner.hasNext()){
						String valType  = type;
					switch (valType) {
//						case "int":
//						case "INT":
//							try {
//                                                            long l = value.length()>0?Long.parseLong(value):null;
//                                                            value = String.valueOf(l);
//                                                                return value;
////								doo.setField(name, value);
//							} catch (Exception e) {
//								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Integer:"+e.getMessage());
//							}
//						case "d":
//						case "D":
//							try {
//                                                            return convertToDate(value).toGMTString();
////								doo.setField(name,convertToDate(value).toGMTString());
//							} catch (Exception e) {
//								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Date:"+e.getMessage());
//							}
						case "f":
						case "F":
							try {
                                                            double d = value.length()>0?Double.parseDouble(value):null;
                                                            BigDecimal bd = BigDecimal.valueOf(d);
                                                            return bd;
//								doo.setField(name, value);
							} catch (Exception e) {
								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Double:"+e.getMessage());
							}

//						case "s":
//						case "S":
//							try {
//                                                            return value;
////								doo.setField(name, value);
//							} catch (Exception e) {
//								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� String:"+e.getMessage());
//							}

						case "i":
						case "I":
						case "l":
						case "L":
							try {
                                                            long l = value.length()>0?Long.parseLong(value):null;
                                                            BigDecimal bd = BigDecimal.valueOf(l);
                                                            return bd;
//								doo.setField(name, value);
							} catch (Exception e) {
								throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Long:"+e.getMessage());
							}
//							break;

						default:
							throw new IllegalArgumentException("�������� "+value+" �������� ����������� ���:"+valType);
						}


					}else{
						throw new IllegalArgumentException("�������� �� �������� ���, ����� ����������� \"|\"");
					}
//				}else{
//					doo.setField(name, null);
//                                        scanner.next();
////                                        throw new IllegalArgumentException("�������� ������");
//				}
//			}
		}

            @Override
            public Object getValue(String value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        },
        BIGDECIMAL {

            @Override
            public Object getValue(String value) {
                if(value.trim().length()>0) {
                    try {
//                        double d = value.length()>0?Double.parseDouble(value):null;
//                        BigDecimal bd = BigDecimal.valueOf(d);
                         BigDecimal bd = new BigDecimal(value);
                        return bd;
//								doo.setField(name, value);
                    } catch (Exception e) {
                            throw new IllegalArgumentException("������ ��������������  "+value+" � ��� BigDecimal:"+e.getMessage());
                    }
                } else return null;
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        },
//	PRIMARY{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {doo.setPrimary() ;}
//	},
//	SUBOBJECT{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {doo.setSubobject() ;}
//	},
//	SUBOBJECT_DEPOLINK{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {doo.setSubobjectWithDepolink() ;}
//	},
//	REF{
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {doo.setRef() ;}
//	},
//	CACHE{
//		@Override
//		protected void setupElement(Scanner scanner,String cacheName,DataObject doo) {doo.setCacheName(cacheName) ;}
//	},
//	TABLE{
//		@Override
//		protected void setupElement(Scanner scanner,String tableName,DataObject doo) {
//                    doo.setTableName(tableName);
//                    doo.setClassName(Config.getInstance().tables.get(tableName).getDataType());
//                    doo.setClassIdName(Config.getInstance().tables.get(tableName).getKeyDataType());
//                }
//	},
        BOOLEAN {
            @Override
            public Object getValue(String value) {
                if(value.trim().length()>0) {
                    try {
                        Boolean bln = null;
                        switch (value) {
                            case "0":
                                bln = false;
                                return bln;
                            case "1":
                                bln= true;
                                return bln;
                            default:
                                    throw new IllegalArgumentException("�������� "+value+" �� ������������� ���������� ��������� ��� BOOLEAN");
                        }
                    } catch (Exception e) {
                            throw new IllegalArgumentException("������ ��������������  "+value+" � ��� Boolean:"+e.getMessage());
                    }
                } else return null;
            }

            @Override
            public Object getValue(String value, long partId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, long partId, long rootId) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object getValue(String value, String type) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        },
//        FLOAT {
//		@Override
//		protected void setupElement(Scanner scanner,String name,DataObject doo) {
//			String s =  scanner.next();
//			try {
//                            doo.setField(name, new BigDecimal(s));
//			} catch (Exception e) {
//				throw new IllegalArgumentException("������ "+s+" �� ���������� ��� �����(long)");
//			}
//		}
//        }
	;
//	public void receiveNextValue(Scanner scanner,String name,DataObject doo) {
//		if(doo==null)throw new IllegalArgumentException("������ ��� ������������� �� �������");
//		if(scanner==null)throw new IllegalArgumentException("�������� ��� ������������� �� �������");
//		setupElement(scanner,name,doo);
//	}
        
        public abstract Object getValue(String value);
        public abstract Object getValue(String value, long partId);
        public abstract Object getValue(String value, long partId, long rootId);
        public abstract Object getValue(String value, String type);
        
        
	protected static Date default_01011900;
	static{
		Calendar calendar = Calendar.getInstance();
		calendar.set(1900, Calendar.JANUARY , 1, 0, 0, 0);
		default_01011900 = calendar.getTime();
	}
	
        private static String convertDateToString(String dateAsStr) throws Exception{
		
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy",Locale.ROOT);
		SimpleDateFormat formatTime = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss",Locale.ROOT);
                formatTime.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		SimpleDateFormat formatTime1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",Locale.ROOT);
                formatTime1.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                SimpleDateFormat formatTimeOut = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.ROOT);
                formatTimeOut.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

		if(dateAsStr.length()<=0 || dateAsStr.startsWith("**.**.****") || dateAsStr.startsWith("T")) return null;
		dateAsStr = dateAsStr.replaceAll("T", " ");
		for(int i=0;i<=2;i++){
			try {
				synchronized (format) {
					return formatTimeOut.format(format.parse(dateAsStr));
				}
			} catch (Exception e) {
				try {
					return	formatTimeOut.format(formatTime.parse(dateAsStr));
				} catch (Exception e2) {
					try {
						return	formatTimeOut.format(formatTime1.parse(dateAsStr));
					} catch (Exception e3) {
						if(i==2)
							throw e3;
					}
				}
			}
		}
		return null;	
	}        
        
	private static Date convertToDate(String dateAsStr) throws Exception{
//		SimpleDateFormat formatTime1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",Locale.ROOT);
//                formatTime1.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
//                Date dt=formatTime1.parse(dateAsStr);
//                return	dt;
            
		return convertToDate(dateAsStr,null);	
	}
	private static Date convertToDate(String dateAsStr,Date ifNullValue) throws Exception{
		
//		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy",Locale.ROOT);
//		SimpleDateFormat formatTime = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss",Locale.ROOT);
//                formatTime.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		SimpleDateFormat formatTime1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",Locale.ROOT);
                formatTime1.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

		if(dateAsStr.length()<=0 || dateAsStr.startsWith("**.**.****") || dateAsStr.startsWith("T")) return ifNullValue;
		dateAsStr = dateAsStr.replaceAll("T", " ");
                
			try {
				synchronized (formatTime1) {
					return	formatTime1.parse(dateAsStr);
				}
			} catch (Exception e) {            
                            
                        }    
//		for(int i=0;i<=2;i++){
//			try {
//				synchronized (format) {
//					return	format.parse(dateAsStr);
//				}
//			} catch (Exception e) {
//				try {
//					return	formatTime.parse(dateAsStr);
//				} catch (Exception e2) {
//					try {
//						return	formatTime1.parse(dateAsStr);
//					} catch (Exception e3) {
//						if(i==2)
//							throw e3;
//					}
//				}
//			}
//		}
		return ifNullValue;	
	}
        
        private static char[] compact(Long key) {
            short mask = 0b0000_0000_0000_0000;
            int capacity = 4;

            long value = key;

            char[] compactKey = new char[capacity];
            for (byte i = 0; i < capacity; i++) {
                char currentChar;
                if (value != 0) {
                    currentChar = (char) value;
                    value = value >> 16;
                } else {
                    currentChar = (char) mask;
                }

                compactKey[i] = currentChar;
            }
            return compactKey;
        }
    
//        public Object getObject (Object obj, String line) {
//            Field[] fields = obj.getClass().getFields();
//            Field field = fields[0];
//            field.getName();
//            
//            
//            return obj;
//        }
}
