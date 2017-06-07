/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.model.based.configurable.nat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Thread.sleep;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
//import jyang.parser.YANG_Body;
//import jyang.parser.YANG_Config;
//import jyang.parser.YANG_Specification;
//import jyang.parser.YangTreeNode;
//import jyang.parser.yang;
//import jyang.tools.Yang2Yin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.TimerTask;
import java.util.Timer;
import org.slf4j.LoggerFactory;


/**
 *
 * @author lara
 */
public class StateListenerNew extends Thread{
    private static final String YINFILE = "configuration/yinFile.txt";
    private static final String YANGFILE = "configuration/yangFile.yang";
    private static final String MAPPINGFILE = "configuration/mappingFile.txt";    
    //protected List<String> state;
    protected HashMap<String, Object> state;
    protected HashMap<String, Object> stateThreshold;
    //protected HashMap<String, ListValues> stateList;
    protected HashMap<String, String> lists;
    private Object root;
    private boolean stopCondition = false;
    private List<String> toListenPush;
    private HashMap<String, Threshold> toListenThreshold;
    private List<PeriodicVariableTask> toListenTimer;
    //private List<String> nullValuesToListen;
    private HashMap<String, String> YangToJava;
    //private List<NotifyMsg> whatHappened;
    //private ReadLock readLock;
    //private WriteLock writeLock;
    private ConnectionModuleClient cM;
    private final ObjectNode rootJson;
    private final ObjectMapper mapper;
    private HashMap<String, Object> stateNew;
    private HashMap<String, Boolean> config;
    private Timer timer;
    protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
 
    public StateListenerNew(Object root){
//        log.info("In constructor");
        this.root = root;
        state = new HashMap<>();
        stateThreshold = new HashMap<>();
        //stateList = new HashMap<>();
        toListenPush = new ArrayList<>();
        toListenThreshold = new HashMap<>();
        toListenTimer = new ArrayList<>();
        //nullValuesToListen = new ArrayList<>();
        YangToJava = new HashMap<>();
        //whatHappened = new ArrayList<>();
        //ReentrantReadWriteLock wHLock = new ReentrantReadWriteLock();
        //readLock = wHLock.readLock();
        //writeLock = wHLock.writeLock();
        lists = new HashMap<>();
        config = new HashMap<>();
        mapper = new ObjectMapper();
        timer = new Timer();
        cM = new ConnectionModuleClient(this, "StateListener");
        //PARSE YANG FILE
        ClassLoader loader = AppComponent.class.getClassLoader();
        try{
            
            
            File yangFile = new File(loader.getResource(YANGFILE).getFile());
            /*new yang(new FileInputStream(yangFile));
            
            YANG_Specification spec = yang.Start();
            //System.out.println(spec);
            spec.check();
            
            File yin = new File("src/main/resources/files/yinFile.txt");
            if(!yin.exists())
                yin.createNewFile();
            new Yang2Yin(spec, new String[0], new PrintStream(yin));
            Vector<YANG_Body> bodies= spec.getBodies();
            YangTreeNode yangTree = spec.getSchemaTree();
            //findYangLeafs(yangTree);
            for(int i=0; i< bodies.size();i++){
                //System.out.println("body "+i);
                //System.out.println(bodies.get(i));
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(yin);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            
            //System.out.println("Root yin "+doc.getDocumentElement().getNodeName());

            NodeList nodes = doc.getElementsByTagName("leaf");
            for(int i=0; i<nodes.getLength();i++){
                Node n = nodes.item(i);
                //System.out.println("node "+i+" "+n);
                    for(int j=0;j<n.getAttributes().getLength();j++)
                        //System.out.println("--Attribute "+n.getAttributes().item(j).getNodeName()+" "+n.getAttributes().item(j).getNodeValue());
                if(n.getNodeType()==Node.ELEMENT_NODE){
                    Element e=(Element)n;
                    NodeList childs = e.getChildNodes();
                    for(int k=0;k<childs.getLength();k++){
                        if(childs.item(k).getNodeType()==Node.ELEMENT_NODE)
                            //System.out.println("++figlio : "+childs.item(k).getNodeName()+" "+childs.item(k).getAttributes().item(0).getNodeValue());
                    }
            }     
                
            }
            
            findYinLeafs(doc.getDocumentElement(), "");*/
            
            InputStream yinFile = loader.getResourceAsStream(YINFILE);
            JsonNode rootYin = mapper.readTree(yinFile);
            
            
//            log.info("read yinFile " +rootYin);
            //System.out.println(rootYin);
            
            findYinLeafs(rootYin, rootYin.get("@name").textValue());
            
        } catch (Exception ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //System.out.println("---CONFIG-----");
        //System.out.println(config);
//        log.info("++CONFIG "+config);
        
        //System.out.println("---toListenPush-----");
        //System.out.println(toListenPush);
//        log.info("--toListenPush "+toListenPush);
        
        //System.out.println("---toListenThreshold-----");
        //System.out.println(toListenThreshold);
//        log.info("--toListenThreshold "+toListenThreshold);
        
        //System.out.println("---toListenTimer-----");
        //System.out.println(toListenTimer);        
//        log.info("--toListenTimer "+toListenTimer);
        
        //PARSE MAPPING FILE
            InputStream mapFile = loader.getResourceAsStream(MAPPINGFILE);
            try(Scanner s = new Scanner(mapFile)){
                while(s.hasNextLine()){
                    String line = s.nextLine();
                    String[] couples = line.split(Pattern.quote(";"));
                    for(int i=0; i<couples.length;i++){
                        String[] yj = couples[i].split(Pattern.quote(":"));
                        if(yj.length==2)
                            YangToJava.put(yj[1].trim(), yj[0].trim());
                    }
                    ////System.out.println(YangToJava.toString());
                }
            }
            //ADD VARIABLES TO LISTEN
            Collection<String> all = YangToJava.keySet();
            List<String> sorted = new ArrayList<String>(all);
            Collections.sort(sorted);
            //System.out.println(sorted);
            List<String> leafs = new ArrayList<>();
            for(int i=0; i<sorted.size()-1; i++){
                String id0 = sorted.get(i);
                String id1 = sorted.get(i+1);
                if(!id1.contains(id0))
                    leafs.add(id0);
            }
            leafs.add(sorted.get(sorted.size()-1));
            for(String l:YangToJava.keySet()){
                if(l.endsWith("]")){
                    String index = l.substring(l.lastIndexOf("[")+1, l.lastIndexOf("]"));
                    String idList = l.substring(0, l.length()-index.length()-2);
                    lists.put(idList.substring(5)+"[]", index);
                }
            }
            rootJson = mapper.createObjectNode();
            for(String l:leafs)
                createTree(rootJson, YangToJava.get(l));
            //System.out.println(leafs);
            //check push-never-threshold-periodic
//            for(String s:leafs){
//                String s1 = s.substring(5);
//                toListenPush.add(s1);
//                //this.addNewListener(s1);
//            }
            this.start();
    }
    
    
    public void run(){
        while(!stopCondition){
            try {
                //System.out.println("Parte il ciclo");
                //checkValue();
                saveNewValues();
                sleep(5000);
            } catch (InterruptedException ex) {
                stopCondition = true;
                cM.deleteResources();
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        log.info("The program has been stopped");
        cM.deleteResources();
    }
    
    private void stopTimerTasks(){
        log.info("Stopping periodicTasks....");
        toListenTimer.forEach((t) -> {
            t.cancel();
        });
        log.info("...Stopped periodic tasks");
    }
    
    public void saveNewValues(){
        stateNew = new HashMap<>();
        for(String s:toListenPush){
            try {
                String sj = fromYangToJava(s);
                saveValues(root, sj.substring(5), sj.substring(5), stateNew);
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ////System.out.println(stateNew);
        checkChangesSaved();
        Map<String, Object> thr = new HashMap<>();
        for(String s:toListenThreshold.keySet()){
            try {
                if(YangToJava.containsValue(s)){
                    String sj = null;
                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(s)){
                            sj = k;
                            break;
                        }
                    saveValues(root, sj.substring(5), sj.substring(5), thr);
                }
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        checkThreshold(thr);
        ////System.out.println("new value of state -- ");
        ////System.out.println(state);
    }
    
    public void saveValues(Object actual, String subToListen, String complete, Map<String, Object> toSave) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        if(subToListen.contains("/")){
            String inter = subToListen.substring(0, subToListen.indexOf("/"));
            if(inter.contains("[")){
                String lName = inter.substring(0, inter.indexOf("["));
                String index = inter.substring(inter.indexOf("[")+1, inter.length()-1);
                actual = actual.getClass().getField(lName).get(actual);
                if(actual!=null){
                    if(List.class.isAssignableFrom(actual.getClass())){
                        for(Object item:(List)actual){
                            String indexValue = searchLeafInList(item, index);
                            String complToPass = complete.substring(0, complete.length()-subToListen.length())+lName+"["+indexValue+"]"+subToListen.substring(inter.length());
                            saveValues(item, subToListen.substring(inter.length()+1), complToPass, toSave);
                        }
                    }else if(Map.class.isAssignableFrom(actual.getClass())){
                        for(Object key:((Map)actual).keySet()){
                            String indexValue = key.toString();
                            String complToPass = complete.substring(0, complete.length()-subToListen.length())+lName+"["+indexValue+"]"+subToListen.substring(inter.length());
                            if(subToListen.substring(inter.length()+1).equals("{key}")){
                                //save the key
                                toSave.put(complToPass, key);
                            }
                            else
                                saveValues(((Map)actual).get(key), subToListen.substring(inter.length()+1), complToPass, toSave);
                        }
                    }else 
                        return;
                }
            }else{
                actual = actual.getClass().getField(inter).get(actual);
                if(actual!=null)
                    saveValues(actual, subToListen.substring(inter.length()+1), complete, toSave);
            }
        }else{
            //leaf
            if(subToListen.contains("[")){
                //è una mappa
                String mapName = subToListen.substring(0, subToListen.indexOf("["));
//                String ind = subToListen.substring(subToListen.indexOf("[")+1, subToListen.indexOf("]"));
                Map mappa = (Map) actual.getClass().getField(mapName).get(actual);
                if(mappa!=null){
                    for(Object k:mappa.keySet()){
                        String complToPass = complete.substring(0, complete.lastIndexOf("[")+1)+k.toString()+"]";
                        toSave.put(complToPass, mappa.get(k));
                    }
                }
            }else{
                if(!subToListen.equals("{value}"))
                    actual = actual.getClass().getField(subToListen).get(actual);
                toSave.put(complete, actual);
            }
        }
    }
    
    private void checkChangesSaved(){
        List<NotifyMsg> happenings = new ArrayList<>();
        HashMap<String, Object> copyState = new HashMap<>();
        HashMap<String, Object> copyNewState = new HashMap<>();
        List<String> ancoraPresenti = new ArrayList<>();
        if(state!=null && stateNew!=null){
            copyState.putAll(state);
            copyNewState.putAll(stateNew);
            for(String k:state.keySet()){
                if(stateNew.containsKey(k)){
                    if(state.get(k)==null){
                        if(stateNew.get(k)!=null){
                            //ADDED
                           NotifyMsg e = new NotifyMsg();
                           e.act=action.ADDED;
                           e.var=trasformInPrint(k);
                           e.obj=stateNew.get(k);
                           happenings.add(e);
                           //System.out.println((new Gson()).toJson(e));
                           log.info((new Gson()).toJson(e));
                        }else{
                            stateNew.remove(k);
                            copyNewState.remove(k);
                            continue;
                        }
                    }
                    if(stateNew.get(k)==null){
                        stateNew.remove(k);
                        copyNewState.remove(k);
                        continue;
                    }
                    //non sono stati eliminati
                    if(!state.get(k).equals(stateNew.get(k))){
                       //CHANGED VALUE
                       NotifyMsg e = new NotifyMsg();
                       e.act=action.UPDATED;
                       e.var=trasformInPrint(k);
                       e.obj=stateNew.get(k);
                       happenings.add(e);
                       //System.out.println((new Gson()).toJson(e));
                       log.info((new Gson()).toJson(e));
                    }
                    copyState.remove(k);
                    copyNewState.remove(k);
                    ancoraPresenti.add(k);
                }
            }
            //update the actual state
            state = stateNew;
            //copyState contains the eliminated
            ObjectNode rootJ = mapper.createObjectNode();
            for(String k:copyState.keySet()){
                NotifyMsg e = new NotifyMsg();
                e.act=action.REMOVED;
                e.obj=copyState.get(k);
                e.var=trasformInPrint(k);
                happenings.add(e);
                insertInNode(rootJ, k, generalIndexes(k), e.obj);
                //System.out.println((new Gson()).toJson(e));
                log.info((new Gson()).toJson(e));
            }
            //System.out.println("REM --");
            //System.out.println(rootJ);

            //copyNewState contains the added
            rootJ = mapper.createObjectNode();
            for(String k:copyNewState.keySet()){
                NotifyMsg e = new NotifyMsg();
                e.act=action.ADDED;
                e.obj=copyNewState.get(k);
                e.var=trasformInPrint(k);
                happenings.add(e);
                insertInNode(rootJ, k, generalIndexes(k), e.obj);
                //System.out.println((new Gson()).toJson(e));
                log.info((new Gson()).toJson(e));
            }
            //System.out.println("ADD--");
            //System.out.println(rootJ);
            
            rootJ = mapper.createObjectNode();
            for(String s:ancoraPresenti)
                insertInNode(rootJ, s, generalIndexes(s), "presente");
            //System.out.println("--Presenti--");
            //System.out.println(rootJ);
        }
        
        for(NotifyMsg e:happenings){
            //System.out.println(e.act + " "+e.var + " "+e.obj);
//            log.info(e.act+" "+e.var+" "+e.obj);
            cM.somethingChanged((new Gson()).toJson(e));
        }
        
    }
    
    private void insertInNode(ObjectNode node, String s, String complete, Object v){
        if(s.contains("/")){
            String f = s.substring(0, s.indexOf("/"));
            String field = (f.contains("["))?f.substring(0, f.indexOf("[")):f;
            String index = (f.contains("["))?f.substring(f.indexOf("[")+1, f.indexOf("]")):null;
            if(node.findValue(field)!=null){
                JsonNode next = node.get(field);
                if(next.isArray()){
                    Iterator<JsonNode> nodes = ((ArrayNode)next).elements();
                    String list = getListName(complete, s);
                    if(lists.containsKey(list)){
                        String ind = lists.get(list);
                        boolean found = false;
                        while(nodes.hasNext()){
                            ObjectNode objN = (ObjectNode)nodes.next();
                            if(objN.findValue(ind)!=null && objN.get(ind).asText().equals(index)){
                                insertInNode(objN, s.substring(s.indexOf("/")+1), complete, v);
                                found = true;
                                break;
                            }
                        }
                        if(found==false){
                            ObjectNode obj = mapper.createObjectNode();
                            obj.put(ind, index);
                            insertInNode(obj, s.substring(s.indexOf("/")+1), complete, v);
                            ((ArrayNode)next).add(obj);
                        }
                    }
                }else{
                    insertInNode((ObjectNode)next, s.substring(s.indexOf("/")+1), complete, v);
                }
            }else{
                if(index==null){
                    ObjectNode next = mapper.createObjectNode();
                    insertInNode(next, s.substring(s.indexOf("/")+1), complete, v);
                    node.put(field, next);
                }else{
                    ArrayNode array = mapper.createArrayNode();
                    String list = getListName(complete, s);
                    if(lists.containsKey(list)){
                        String ind = lists.get(list);
                        ObjectNode next = mapper.createObjectNode();
                        next.put(ind, index);
                        insertInNode(next, s.substring(s.indexOf("/")+1), complete, v);
                        array.add(next);
                    }
                    node.put(field, array);
                }
            }
        }else{
            if((node.findValue(s))==null && v!=null)
                node.put(s, v.toString());
        }
    }
    
    private String getListName(String complete, String last){
        String[] c = complete.split(Pattern.quote("/"));
        String[] l = last.split(Pattern.quote("/"));
        String res =new String();
        for(int i=0;i<c.length-l.length+1;i++)
            res+=c[i]+"/";
        res = res.substring(0,res.lastIndexOf("[")+1)+"]";
        return res;
    }

    private String trasformInPrint(String var) {
        String[] partsWithoutIndex = var.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String j=partsWithoutIndex[0];
        String onlyLastOne = partsWithoutIndex[0];
        String y=null;
        if(partsWithoutIndex.length>1)
            for(int i=1;i<partsWithoutIndex.length;i++){
                if(i%2==0){
                    //nome lista
                    j+=partsWithoutIndex[i];
                    onlyLastOne+=partsWithoutIndex[i];
                }else{
                    if(lists.containsKey(j+"[]"))
                        j+="["+lists.get(j+"[]")+"]";
                    if(i==partsWithoutIndex.length-1){
                        if(lists.containsKey(j+"[]"))
                            onlyLastOne+=("["+lists.get(j+"[]")+"]");
                    }
                    else
                        onlyLastOne+=("["+partsWithoutIndex[i]+"]");
                            
                }
            }
        String toVerify = "root/"+j;
        for(String s:YangToJava.keySet())
            if(s.equals(toVerify))
                    y=YangToJava.get("root/"+j);
        if(y!=null){
            String[] yparse = y.split(Pattern.quote("[]"));
            String toPub=new String();
            for(int i=0; i<partsWithoutIndex.length;i++){
                if(i%2==0)
                    toPub+= yparse[i/2];
                else
                    toPub+="["+partsWithoutIndex[i]+"]";
            }
            return toPub;
        }
        return y;
    }
     
    private JsonNode getCorrectItem(String newVal, String complete){
        //complete in Yang
        //newVal in Yang
        try{
            JsonNode node = mapper.readTree(newVal);
            JsonNode newNode;
            if(node.isObject()){
                newNode = mapper.createObjectNode();
                Iterator<String> fields = node.fieldNames();
                while(fields.hasNext()){
                    String fieldJava = null;
                    String fieldName = (String)fields.next();
                    if(YangToJava.containsValue(complete+"/"+fieldName))
                        for(String k:YangToJava.keySet())
                            if(YangToJava.get(k).equals(complete+"/"+fieldName))
                                fieldJava=k;
                    if(fieldJava!=null){
                        fieldJava=fieldJava.substring(fieldJava.lastIndexOf("/")+1);
                        if(node.get(fieldName).isValueNode())
                            ((ObjectNode)newNode).put(fieldJava, node.get(fieldName));
                        else{
                            String newCampo = (node.get(fieldName).isObject())?complete+"/"+fieldName:complete+"/"+fieldName+"[]";
                            JsonNode subItem = getCorrectItem(mapper.writeValueAsString(node.get(fieldName)),complete+"/"+fieldName);
                            ((ObjectNode)newNode).put(fieldJava, subItem);
                        }
                    }
                }
            }else{
                newNode = mapper.createArrayNode();
                Iterator<JsonNode> iter = ((ArrayNode)node).elements();
                while(iter.hasNext()){
                    JsonNode item = iter.next();
                    JsonNode subItem = getCorrectItem(mapper.writeValueAsString(item),complete+"[]");
                    ((ArrayNode)newNode).add(subItem);
                }
            }
            return newNode;
        }catch(IOException ex){
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void createTree(JsonNode node, String l) {
        if(l==null || l.equals(""))
            return;
        String[] splitted = l.split(Pattern.quote("/"));
        if(node.isObject()){
            JsonNode next;
            if(splitted[0].contains("[")){
                String inter = splitted[0].substring(0, splitted[0].indexOf("["));
                next = ((ObjectNode)node).get(inter);
                if(next==null)
                    ((ObjectNode)node).put(inter, mapper.createArrayNode());
                next = ((ObjectNode)node).get(inter);
            }else{
                next = ((ObjectNode)node).get(splitted[0]);
                if(next==null){
                    if(splitted.length>1 || (splitted.length>1&&splitted[1].contains("[")))
                        ((ObjectNode)node).put(splitted[0], mapper.createObjectNode());
                    else if (splitted.length==1 && splitted[0].contains("[")){
                        ArrayNode mappa = mapper.createArrayNode();
                        ObjectNode elemMappa = mapper.createObjectNode();
                        elemMappa.put("key", "");
                        elemMappa.put("value", "");
                        ((ObjectNode)node).put(splitted[0], mappa);
                    }else
                       ((ObjectNode)node).put(splitted[0], new String()); 
                }
                next = ((ObjectNode)node).get(splitted[0]);
            }
            if(splitted.length>1)
                createTree(next, l.substring(splitted[0].length()+1));
            if(splitted.length==1&&next.isArray()){
//                ObjectNode elemMappa = mapper.createObjectNode();
//                elemMappa.put("key", "");
//                elemMappa.put("value", "");
//                ((ArrayNode)next).add(elemMappa);
            }
        }else{
            JsonNode next;
            if(splitted[0].contains("[")){
                String inter = splitted[0].substring(0, splitted[0].indexOf("["));
                if(node.isArray()){
                    //è una lista
                    if(((ArrayNode)node).elements().hasNext()==false)
                        ((ArrayNode)node).addObject();
                    next = ((ArrayNode)node).get(0);
                    if(((ObjectNode)next).get(inter)==null)
                        ((ObjectNode)next).put(inter, mapper.createArrayNode());
                    next = ((ObjectNode)next).get(inter);
                }else{
                    //è l'elemento di una mappa
                    ArrayNode newNode = mapper.createArrayNode();
                    ObjectNode nn = mapper.createObjectNode();
                    nn.put("id", "");
                    nn.put("value", nn);
                    newNode.add(nn);
                    return;
                }
            }else{
                if(((ArrayNode)node).elements().hasNext()==false)
                    ((ArrayNode)node).addObject();
                next = ((ArrayNode)node).get(0);
                if(((ObjectNode)next).get(splitted[0])==null){
                    if(splitted.length>2)
                        ((ObjectNode)next).put(splitted[0], mapper.createObjectNode());
                    else
                       ((ObjectNode)next).put(splitted[0], new String()); 
                }
                next = ((ObjectNode)next).get(splitted[0]);
            }
            if(splitted.length>1)
                createTree(next, l.substring(splitted[0].length()+1));
        }
    }

    public JsonNode getComplexObj(String var) throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        String[] spl = var.split(Pattern.quote("/"));
        JsonNode ref = rootJson;
        for(int i=0;i<spl.length;i++){
            String field =(spl[i].contains("["))?spl[i].substring(0, spl[i].indexOf("[")):spl[i];
            String index =(spl[i].contains("["))?spl[i].substring(spl[i].indexOf("[")+1, spl[i].indexOf("]")):null;
            if(ref.isObject()){
                if(((ObjectNode)ref).get(field)!=null){
                    ref = ((ObjectNode)ref).get(field);
                    if(index!=null && !index.equals("")){
                        ref=((ArrayNode)ref).get(0);
                    }if(index!=null && index.equals("")&& i!=spl.length-1)
                        return null;
                    continue;
                }else{
                    //System.out.println(var + " not found");
                    return null;
                }
            }else{
                if(((ArrayNode)ref).elements().hasNext()==false){
                    //System.out.println(var + " not found-array version");
                    return null;
                }
                ref = ((ArrayNode)ref).get(0);
                if(((ObjectNode)ref).get(field)==null){
                    //System.out.println(var +" not found!");
                    return null;
                }
                continue;
            }
        }
//        log.info("Created tree: "+ref);
        //System.out.println(ref);
        if(ref.isValueNode()){
            //is a leaf, but it is not present in state
            String varJava = fromYangToJava(var);
            Object value = getLeafValue(varJava.substring(5));
            ObjectNode result = mapper.createObjectNode();
            result.put(var.substring(var.lastIndexOf("/")+1), (new Gson()).toJson(value));
            return result;
        }
        JsonNode res;// = (ref.isObject())?mapper.createObjectNode():mapper.createArrayNode();
        var=(ref.isArray()&&var.endsWith("[]"))?var.substring(0, var.length()-2):var;
        res = fillResult(ref, var);
        //System.out.println(res);
//        log.info("The result is "+res);
        JsonNode r = mapper.createObjectNode();
        ((ObjectNode)r).put(var.substring(var.lastIndexOf("/")+1), res);
//        log.info("The result is ready");
        return r;
    }

    private JsonNode fillResult(JsonNode ref, String var) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        JsonNode toRet;
        if(ref.isObject()){
            //fill fields
            toRet = mapper.createObjectNode();
            Iterator<String> field = ((ObjectNode)ref).fieldNames();
            if(!field.hasNext()){
                //searchCorrispondentField
                String varWithoutIndexes = new String();
                String[] varSp = var.split("["+Pattern.quote("[]")+"]");
                for(int i=0; i<varSp.length;i++)
                    if(i%2==0)
                        varWithoutIndexes+=varSp[i]+"[]";
                varWithoutIndexes = varWithoutIndexes.substring(0, varWithoutIndexes.length()-2);
                if(YangToJava.containsValue(varWithoutIndexes)){
                    String key = null;
                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(varWithoutIndexes))
                            key = k;
                    String[] yspez = var.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String[] jspez = key.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String jWithIndex = new String();
                    for(int i=0;i<yspez.length;i++){
                        if(i%2==0)
                            jWithIndex+=jspez[i];
                        else
                            jWithIndex+="["+yspez[i]+"]";
                    }
                    ((ObjectNode)toRet).put(var, getLeafValue(jWithIndex.substring(5)).toString());
                }
                return toRet;
            }
            while(field.hasNext()){
                String fieldName = field.next();
                if(((ObjectNode)ref).get(fieldName).isValueNode()){
                    String varWithoutIndexes = new String();
                    String[] varSp = (var+"/"+fieldName).split("["+Pattern.quote("[]")+"]");
                    for(int i=0; i<varSp.length;i++)
                        if(i%2==0)
                            varWithoutIndexes+=varSp[i]+"[]";
                    varWithoutIndexes = varWithoutIndexes.substring(0, varWithoutIndexes.length()-2);
                    if(YangToJava.containsValue(varWithoutIndexes)){
                        String key = null;
                        for(String k:YangToJava.keySet())
                            if(YangToJava.get(k).equals(varWithoutIndexes))
                                key = k;
                        String[] yspez = (var+"/"+fieldName).split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                        String[] jspez = key.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                        String jWithIndex = new String();
                        for(int i=0;i<yspez.length;i++){
                            if(i%2==0)
                                jWithIndex+=jspez[i];
                            else
                                jWithIndex+="["+yspez[i]+"]";
                        }
                        Object value = getLeafValue(jWithIndex.substring(5));
                        if(value!=null)
                            ((ObjectNode)toRet).put(fieldName, value.toString());
                    }
                }else{
                    JsonNode f = fillResult(((ObjectNode)ref).get(fieldName), var+"/"+fieldName);
                    if(f.size()!=0)
                        ((ObjectNode)toRet).put(fieldName, f);
                }
            }
            return toRet;
        }else{
            
            //add elements
            String listWithoutIndex = noIndexes(var);
//                    new String();
//                String[] varSp = var.split("["+Pattern.quote("[]")+"]");
//                for(int i=0; i<varSp.length;i++)
//                    if(i%2==0)
//                        listWithoutIndex+=varSp[i]+"[]";
//                listWithoutIndex = listWithoutIndex.substring(0, listWithoutIndex.length()-2);
            toRet = mapper.createArrayNode();
            String listInJava = null;
            for(String l:YangToJava.keySet()){
                if(YangToJava.get(l).contains(listWithoutIndex+"[") && YangToJava.get(l).substring(0, listWithoutIndex.length()+1).equals(listWithoutIndex+"[")){
                    String rem = YangToJava.get(l).substring(listWithoutIndex.length());
                    if(!rem.contains("/"))
                            listInJava = l;
                }
            }
            String[] yspez = var.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
            String[] jspez = listInJava.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
            String jWithIndex = new String();
            for(int i=0;i<yspez.length;i++){
                if(i%2==0)
                    jWithIndex+=jspez[i];
                else
                    jWithIndex+="["+yspez[i]+"]";
            }
            //ListValues e = stateList.get(jWithIndex.substring(5)+"[]");
            String lN = generalIndexes(jWithIndex.substring(5))+"[]";
            String e = (lists.containsKey(lN))?lists.get(lN):null;
            if(e!=null){
                String indice=e;
                Object list = getLists(root, jWithIndex.substring(5)+"[]", jWithIndex.substring(5)+"[]");
                if(list!=null && List.class.isAssignableFrom(list.getClass())){
                    List<Object> elems = new ArrayList<>();
                    elems.addAll((List)list);
                    for(Object obj:elems){
                        String idItem = searchLeafInList(obj, indice);
                        JsonNode child = fillResult(((ArrayNode)ref).get(0), var+"["+idItem+"]");
                        if(child.size()!=0)
                            ((ArrayNode)toRet).add(child);
                    }
                }else if(list!=null && Map.class.isAssignableFrom(list.getClass())){
                    Map<Object, Object> elems = new HashMap<>();
                    elems.putAll((Map)list);
                    for(Object k:elems.keySet()){
                        JsonNode child = fillResult(((ArrayNode)ref).get(0), var+"["+k+"]");
                        if(child.size()!=0)
                            ((ArrayNode)toRet).add(child);
                    }
                }
//                if(e.List!=null)elems.addAll(e.List);
//                for(Object obj:elems){
//                    String idItem = searchLeafInList(obj, indice);
//                    ((ArrayNode)toRet).add(fillResult(((ArrayNode)ref).get(0), var+"["+idItem+"]"));
//                }
//                    
            }
                
            return toRet;
        }
    }

    private void setComplexObject(String var, String newVal) {
        try {
            JsonNode toSet = mapper.readTree(newVal);
            //System.out.println(toSet);
//            log.info("toSet is "+toSet);
            //check if all the values are configurable
            if(!configVariables(noIndexes(var), toSet)){
//                log.info("not to config..");
                return;
            }
//            if(!configVariables(var))
//                return;
            fillVariables(toSet, var);
        } catch (IOException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }catch(NoSuchFieldException ex){
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IllegalAccessException ex){
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    private boolean configVariables(String var, JsonNode toSet){
        if(toSet.isValueNode()){
//            log.info("Is a value Node: "+var);
            return config.get(var);
        }
        if(toSet.isObject()){
            Iterator<Entry<String, JsonNode>> iter = ((ObjectNode)toSet).fields();
            boolean ok = true;
            while(iter.hasNext()){
                Entry<String, JsonNode> field = iter.next();
                if(field.getValue().isValueNode()){
                    //leaf - check config
                    if(config.containsKey(var+"/"+field.getKey()))
                        ok = ok && config.get(var+"/"+field.getKey());
                    else{
//                        log.info("Config non contiene "+var+"/"+field.getKey());
                        ok = true;
                    }
                }else
                    ok = ok && configVariables(var+"/"+field.getKey(), field.getValue());
            }
            return ok;
            
        }else{
            Iterator<JsonNode> children = ((ArrayNode)toSet).elements();
            boolean ok = true;
            while(children.hasNext()){
                var = (var.endsWith("]"))?var : var+"[]";
                ok = ok && configVariables(var, children.next());
            }
            return ok;
        }
    }
    
    private boolean configVariables(String var){
        var = deleteIndexes(var);
        String[] fields = var.split(Pattern.quote("/"));
        JsonNode n = rootJson;
        for(int i=0;i<fields.length;i++){
            if(fields[i].contains("[]"))
                fields[i] = fields[i].substring(0, fields[i].length()-2);
            if(n.isArray()){
                n = n.get(0);
                n = ((ObjectNode)n).get(fields[i]);
            }else{
                n = ((ObjectNode)n).get(fields[i]);
            }
        }
//        var = var.replace("/", "/");
        boolean c = checkConfig(n, var);
        return c;
    }
    
    private boolean checkConfig(JsonNode n, String v){
        if(n.isValueNode()){
            if(config.containsKey(v))
                return config.get(v);
            return false;
        }
        if(n.isArray()){
            n = n.get(0);
            v = (v.endsWith("]"))?v:v+"[]";
            return checkConfig(n, v);
        }else{
            Iterator<String> it = ((ObjectNode)n).fieldNames();
            boolean cc = true;
            while(it.hasNext()){
                String fName = (String)it.next();
                cc = cc && checkConfig(n.get(fName), v+"/"+fName);
            }
            return cc;            
        }
    }
    
    private void fillVariables(JsonNode toSet, String var) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
//        log.info("In fillVariables");
        if(toSet.isValueNode()){
//            log.info("In fillVariables - reached leaf");
            //set the corrispondent leaf
            String j = fromYangToJava(var);
            //if(state.containsKey(j.substring(5))){
//            log.info("And variable is "+j);
            if(j!=null)
                setVariable(j.substring(5), j.substring(5), toSet.asText(), root);
            //}
        }else{
            if(toSet.isObject()){
                Iterator<String> fields = toSet.fieldNames();
                while(fields.hasNext()){
                    String fieldName = (String)fields.next();
                    fillVariables(toSet.get(fieldName), var+"/"+fieldName);
                }
            }else{
                //capire qual è la lista corrispondente
                //without indexes
                String varWithoutIndexes = new String();
                String[] varSp = var.split("["+Pattern.quote("[]")+"]");
                for(int i=0; i<varSp.length;i++)
                    if(i%2==0)
                        varWithoutIndexes+=varSp[i]+"[]";
                varWithoutIndexes = varWithoutIndexes.substring(0, varWithoutIndexes.length()-2);
                if(YangToJava.containsValue(varWithoutIndexes)){
                    String key = null;
                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(varWithoutIndexes))
                            key = k;
                    String[] yspez = var.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String[] jspez = key.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String jWithIndex = new String();
                    for(int i=0;i<yspez.length;i++){
                        if(i%2==0)
                            jWithIndex+=jspez[i];
                        else
                            jWithIndex+="["+yspez[i]+"]";
                    }
                    jWithIndex = jWithIndex.substring(5);
                    //crearne una nuova
                    Class<?> type=null;
                    String indice = null;
                    String jgen = generalIndexes(jWithIndex);
                    if(lists.containsKey(jgen+"[]")){
                        indice = lists.get(jgen+"[]");
                        Object actual = root;
                        String[] fields = jWithIndex.split(Pattern.quote("/"));
                        Field f = actual.getClass().getDeclaredField(fields[0]);
                        for(int i=1;i<fields.length;i++){
                            if(fields[i].contains("[")){
                                if(java.util.List.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i].substring(0, fields[i].indexOf("[")));
                                }else if(Map.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i].substring(0, fields[i].indexOf("[")));
                                }else
                                    f = f.getType().getDeclaredField(fields[i].substring(0, fields[i].indexOf("[")));
                            }else{
                                if(java.util.List.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i]);
                                }else if(Map.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i].substring(0, fields[i].indexOf("[")));
                                }else
                                    f = f.getType().getDeclaredField(fields[i]);
                            }
                        }
                        ParameterizedType pt = (ParameterizedType)f.getGenericType();
                        type = (Class<?>)pt.getActualTypeArguments()[0];
                        
//                        for(String s:YangToJava.keySet()){
//                            if(s.contains(key) && s.length()>key.length() && s.substring(0, key.length()+1).equals(key+"[")){
//                                indice = s.substring(key.length()+1);
//                                indice = indice.substring(0, indice.indexOf("]"));
//                                break;
//                            }
//                        }
                    }
                    //setVariable(jWithIndex, jWithIndex, null, root);
                    List<Object> newList = new ArrayList<>();
                    
                        Iterator<JsonNode> iter = ((ArrayNode)toSet).elements();
                        while(iter.hasNext()){                     
                            //insert the list element
                            JsonNode newValJava = getCorrectItem(mapper.writeValueAsString(iter.next()), varWithoutIndexes+"[]");
                            if(newValJava!=null)
                                setVariable(jWithIndex+"[]", jWithIndex+"[]",mapper.writeValueAsString(newValJava), root);
                           
                        }                  
                }
            }
        }
    }

    private String noIndexes(String s){
        String[] split = s.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String ret = new String();
        for(int i=0;i<split.length;i++){
            if(i%2==0)
                ret+=split[i]+"[]";
        }
        if(!s.endsWith("]"))
            ret = ret.substring(0, ret.length()-2);
        return ret;
    }
        
    private String generalIndexes(String s){
        String[] split = s.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String l = new String();
        for(int i=0;i<split.length;i++){
            if(i%2==0){
                l+=split[i];
            }else{
                if(lists.containsKey(l+"[]")){
                    String ind = lists.get(l+"[]");
                    l+="["+ind+"]";
                }
            }
        }
        return l;
    }
    
    
    private String deleteIndexes(String var){
        String[] parts = var.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String res = new String();
        for(int i=0;i<parts.length;i++)
            if(i%2==0)
                res+=parts[i]+"[]";
        if(!var.endsWith("]"))
            res=res.substring(0, res.length()-2);
        return res;
    }
    
    public void parseCommand(String msgJson) throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException{
        CommandMsg msg = ((new Gson()).fromJson(msgJson, CommandMsg.class));
        String var = fromYangToJava(msg.var);
        switch(msg.act){
            case GET:
                //System.out.println("devo passare "+var);
//                log.info("Arrived command GET of "+var);
                log.info("Arrived from ConnectionModule the command GET for "+msg.var);
//                log.info("Translated in "+var);
//                if(var==null)
//                    msg.obj=null;
                if(var!=null && !var.equals("root") && state.containsKey(var.substring(5))){
//                    log.info("Is a Leaf");
                    ObjectNode on= mapper.createObjectNode();
                    String field = (msg.var.contains("/"))?msg.var.substring(msg.var.lastIndexOf("/")+1):msg.var;
                    on.put(field, getLeafValue(var.substring(5)).toString());
                   msg.objret = mapper.writeValueAsString(on);
//                   log.info("Leaf value "+msg.objret);
                   //System.out.println("RESULT GET: E' una foglia "+msg.objret);
                }
                else{
            
                //creare oggetto da passare!
                JsonNode result;
//                log.info("IT's not a leaf");
                //String field = (msg.var.contains("/"))?msg.var.substring(msg.var.lastIndexOf("/")+1):msg.var;
                result = getComplexObj(msg.var);
                
                msg.objret = mapper.writeValueAsString(result);
//                log.info("Result value "+msg.objret);
                //System.out.println("RESULT GET: "+msg.objret);
            
                }
                cM.setResourceValue((new Gson().toJson(msg)));
                break;
            case CONFIG:
                log.info("Arrived from ConnectionModule the command CONFIG for "+msg.var);
                String noInd = deleteIndexes(msg.var);
                if(config.containsKey(noInd) && !config.get(noInd)){
                    //no configurable
                    return;
                }
                try {
                    if(var!=null){
                        //case 1: is a leaf - it is configurable (no configurable leafs are handled in the previous if)
                        if(!var.equals("root")&&state.containsKey(var.substring(5))){
//                            log.info("Config a leaf "+var);
                            setVariable(var.substring(5), var.substring(5), (String)msg.obj, root);
//                            log.info("Leaf should be configured");
                        }else{
//                            log.info("Config a complex object");
                            setComplexObject(msg.var, (String)msg.obj);
//                            log.info("complex object should be configured");
                        }
                    }
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case DELETE:
                //delete
                log.info("Arrived from ConnectionModule the command DELETE for "+msg.var);
                try{
                    if(var==null || var.equals("root")){
                        //System.out.println("Can't delete the root obj!");
                    }else
                        deleteVariable(root, var.substring(5), var.substring(5));
                } catch (NoSuchFieldException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
                break;
        }
    }
    
    public void deleteVariable(Object actual, String var, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String[] fs = var.split(Pattern.quote("/"));
        if(fs.length==1){
            //delete
            if(var.contains("[")){
                //delete an element of the list
                String index = var.substring(var.lastIndexOf("[")+1, var.lastIndexOf("]"));
                if(index!=null && index.matches("")){
                    Field f = actual.getClass().getField(var.substring(0,var.length()-2));
                    f.set(actual, null);
                    return;
                }
                String listName = complete.substring(0, complete.length()-index.length()-1);
                listName+="]";
                listName = generalIndexes(listName)+"[]";
                String indice = null;
                if(lists.containsKey(listName))
                    indice = lists.get(listName);
                if(indice!=null){
                    actual = actual.getClass().getField(var.substring(0, var.lastIndexOf("["))).get(actual);
                    Object delete = null;
                    if(List.class.isAssignableFrom(actual.getClass())){
                    for(Object item:(List)actual){
                        if(item.getClass().getField(indice).get(item).toString().equals(index)){
                            delete = item;
                            break;
                        }
                    }
                    if(delete!=null) ((List)actual).remove(delete);
                    }else if(Map.class.isAssignableFrom(actual.getClass())){
                        if(((Map)actual).containsKey(index))
                           ((Map)actual).remove(index);
                        else{
                            for(Object k:((Map)actual).keySet())
                                if(k.toString().equals(index)){
                                    delete = k; break;}
                            if(delete!=null)
                                ((Map)actual).remove(delete);
                        }
                    }
                }
            }else{
                Field f = actual.getClass().getField(var);
                f.set(actual, null);
            }
        }else{
            //enter
            if(fs[0].contains("[")){
                String fName = fs[0].substring(0, fs[0].indexOf("["));
                String index = fs[0].substring(fs[0].indexOf("[")+1, fs[0].length()-1);
                actual = actual = actual.getClass().getField(fName).get(actual);
                String listName = complete.substring(0, complete.length()-var.length()+fName.length());
                String indice = null;
                listName = generalIndexes(listName);
                if(lists.containsKey(listName+"[]"))
                    indice = lists.get(listName+"[]");
                if(actual!=null){
                    if(List.class.isAssignableFrom(actual.getClass())){
                        for(Object item:(List)actual)
                            if(item.getClass().getField(indice).get(item).toString().equals(index))
                                deleteVariable(item, var.substring(fs[0].length()+1), complete);
                    }else if(Map.class.isAssignableFrom(actual.getClass())){
                        for(Object k:((Map)actual).keySet())
                            if(k.toString().equals(index))
                                deleteVariable(((Map)actual).get(k), var.substring(fs[0].length()+1), complete);
                
                    }
                }
            }else{
                actual = actual.getClass().getField(fs[0]).get(actual);
                if(actual!=null)
                    deleteVariable(actual, var.substring(fs[0].length()+1), complete);
            }
        }
    }
    
    public boolean setVariable(String var, String complete, String newVal, Object actual) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        String[] fs = var.split(Pattern.quote("/"));
        if(fs.length==1){
            //to set
            if(var.contains("[")){
                String index = var.substring(var.indexOf("[")+1, var.indexOf("]"));
                Field f = actual.getClass().getField(var.substring(0, var.lastIndexOf("[")));
                ParameterizedType pt = (ParameterizedType)f.getGenericType();
                Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                if(List.class.isAssignableFrom(f.getType())){
                    if(f.get(actual)==null){
                        try {
                            List<Object> l = (f.getType().isInterface())?new ArrayList<>():(List)f.getType().newInstance();
                            l.add((new Gson()).fromJson(newVal, itemType));
                            f.set(actual, l);
                        } catch (InstantiationException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                            log.info(ex.getMessage());
                        }
                    }else if(index.matches("")){
                        //List<Object> newList = (new Gson()).fromJson(newVal, List.class);
                        ((List)f.get(actual)).add((new Gson()).fromJson(newVal, itemType));
                    }else{
                        String listName = complete.substring(0, complete.length()-index.length()-1);
                        listName = generalIndexes(listName)+"[]";
                        listName+="]";
                        if(lists.containsKey(listName)){
                            Object toChange = null;
                            String indice = lists.get(listName);
                            List<Object> l = (List)f.get(actual);
                            for(Object item:l){
                                if(item.getClass().getField(indice).get(item).toString().equals(index)){
                                    toChange = item;
                                    break;
                                }
                            }
                            if(toChange!=null){
                                l.add((new Gson()).fromJson(newVal, itemType));
                                l.remove(toChange);
                            }
                        }
                    }
                }else if(Map.class.isAssignableFrom(f.getType())){
                    if(f.get(actual)==null){
                        try {
                            Map<Object, Object> m = (f.getType().isInterface())?new HashMap<>():(Map)f.getType().newInstance();
                            Class<?> valueType = (Class<?>)pt.getActualTypeArguments()[1];
                            ObjectNode node = (ObjectNode)mapper.readTree(newVal);
                            JsonNode kNode = node.get("{key}");
                            node.remove("{key}");
                            Object k = (Number.class.isAssignableFrom(itemType))?kNode.asLong():kNode.asText();
                            Object value = valueType.newInstance();
                            Iterator<String> fields = node.fieldNames();
                            while(fields.hasNext()){
                                String fieldName = fields.next();
                                JsonNode v = node.get(fieldName);
                                Field fV = value.getClass().getField(fieldName);
                                if(Number.class.isAssignableFrom(fV.getType()))
                                    fV.set(value, v.asDouble());
                                else
                                    fV.set(value, v.asText());
                            }
                            //value = ((new Gson()).fromJson(mapper.writeValueAsString(node), valueType));
                            m.put(k, value);
                            f.set(actual, m);
                            //!!
                            //m.put((new Gson()).fromJson(newVal, Map.Entry<Object,itemType>));
                            //System.out.println("SETTED M = "+f.get(actual));
                        } catch (InstantiationException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else if(index.matches("")){
                        try{
                            Class<?> valueType = (Class<?>)pt.getActualTypeArguments()[1];
                            ObjectNode node = (ObjectNode)mapper.readTree(newVal);
                            JsonNode kNode = node.get("{key}");
                            node.remove("{key}");
                            Object k = (Number.class.isAssignableFrom(itemType))?kNode.asLong():kNode.asText();
                            Object value = valueType.newInstance();
                            Iterator<String> fields = node.fieldNames();
                            while(fields.hasNext()){
                                String fieldName = fields.next();
                                JsonNode v = node.get(fieldName);
                                Field fV = value.getClass().getField(fieldName);
                                if(Number.class.isAssignableFrom(fV.getType()))
                                    fV.set(value, v.asDouble());
                                else
                                    fV.set(value, v.asText());
                            }
                            //value = ((new Gson()).fromJson(mapper.writeValueAsString(node), valueType));
                            ((Map)f.get(actual)).put(k, value);
                        } catch (IOException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                            log.info(ex.getMessage());
                        } catch (InstantiationException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                            log.info(ex.getMessage());
                        }
                        //((Map)f.get(actual)).put((new Gson()).fromJson(newVal, itemType));
                    }else{
                        String listName = complete.substring(0, complete.length()-index.length()-1);
                        listName = generalIndexes(listName)+"[]";
                        listName+="]";
                        if(lists.containsKey(listName)){
                            Object toChange = null;
                            String indice = lists.get(listName);
                            if(List.class.isAssignableFrom(f.getType())){
                                List<Object> l = (List)f.get(actual);
                                for(Object item:l){
                                    if(item.getClass().getField(indice).get(item).toString().equals(index)){
                                        toChange = item;
                                        break;
                                    }
                                }
                                if(toChange!=null){
                                    l.add((new Gson()).fromJson(newVal, itemType));
                                    l.remove(toChange);
                                }
                            }else if(Map.class.isAssignableFrom(f.getType())){
                                Map<Object, Object> l = (Map)f.get(actual);
                                for(Object item:l.keySet()){
                                    if(item.toString().equals(index)){
                                        toChange = item;
                                        break;
                                    }
                                }
                                if(toChange!=null){    
                                    
                                    //l.put((new Gson()).fromJson(newVal, new TypeToken<l>(){}.getType()));
                                    l.remove(toChange);
                                }                                
                            }
                        }
                    }                    
                }
                   
            }else{
                Field f = actual.getClass().getField(var);
//                log.info("--Arrivata al field da configurare "+f.getName()+" "+f.getGenericType());
//                log.info("Valore: "+newVal);
                f.set(actual, (new Gson()).fromJson(newVal, f.getGenericType()));
//                log.info("okk settato");
            }
        }else{
            if(fs[0].contains("[")){
                //select element in the list
                String listName = complete.substring(0, complete.length()-var.length()+fs[0].length());
                String idItem = listName.substring(listName.lastIndexOf("[")+1, listName.lastIndexOf("]"));
                listName = listName.substring(0, listName.length()-idItem.length()-2)+"[]";
                listName = generalIndexes(listName)+"[]";
                String indice = null;
                if(lists.containsKey(listName))
                    indice = lists.get(listName);
                actual = actual.getClass().getField(fs[0].substring(0, fs[0].length()-idItem.length()-2)).get(actual);
                if(List.class.isAssignableFrom(actual.getClass())){
                for(Object litem:(List)actual){
                    boolean correct = litem.getClass().getField(indice).get(litem).toString().equals(idItem);
                    if(correct)
                        setVariable(var.substring(fs[0].length()+1), complete, newVal, litem);
                }
                }else if(Map.class.isAssignableFrom(actual.getClass())){
                for(Object litem:((Map)actual).keySet()){
                    boolean correct = litem.toString().equals(idItem);
                    if(correct)
                        setVariable(var.substring(fs[0].length()+1), complete, newVal, ((Map)actual).get(litem));
                }
                }
            }else{
                Field f = actual.getClass().getField(fs[0]);
//                log.info("Passing throug "+f.getGenericType());
                actual = f.get(actual);
                setVariable(var.substring(fs[0].length()+1), complete, newVal, actual);
            }
        }
        return false;
    }
    
    public void stopSL(){
        stopTimerTasks();
        stopCondition = true;
        
    } 
    
    
    //returns the id value of the given item of the list
    private String searchLeafInList(Object actual, String idLista) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException{
        String id = null;
        Field[] fs = actual.getClass().getFields();
        for(int i=0; i<fs.length;i++)
            if(fs[i].getName().equals(idLista)){
                Object f = actual.getClass().getField(idLista).get(actual);
                id = (f==null)?null:f.toString();
                return id;
            }        
        return idLista;
    }
    
    //get the value of a specific leaf
    public Object getLeafValue(String id){
        if(state.containsKey(id))
            return state.get(id);
        try{
            Object actual = root;
            String[] fields = id.split(Pattern.quote("/"));
            String recompose = new String();
            for(int i = 0; i<fields.length; i++){
                recompose +="/"+fields[i];
                if(fields[i].contains("[")){
                    String field = fields[i].substring(0, fields[i].lastIndexOf("["));
                    String index = fields[i].substring(fields[i].lastIndexOf("[")+1, fields[i].lastIndexOf("]"));
                    actual = actual.getClass().getField(field).get(actual);
                    if(Map.class.isAssignableFrom(actual.getClass())){
                        if(i<fields.length-1 && fields[i+1].equals("{key}"))
                            actual = index;
                        else
                        actual= ((Map)actual).get(index);
                    }else{
                        String general = generalIndexes(recompose.substring(1));
                        actual = getListItemWithIndex((List)actual,index, general.substring(0, general.lastIndexOf("["))+"[]");
                    }
                }else{
                    if(fields[i].equals("{key}"))
                        continue;
                    if(fields[i].equals("{value}"))
                        continue;
                    actual = actual.getClass().getField(fields[i]).get(actual);
                }
            }
            return actual;
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String fromYangToJava(String y){
        String[] separated = y.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String yang = new String();
        for(int i=0; i<separated.length;i++)
            if(i%2==0 && i!=separated.length-1)
                yang+=separated[i]+"[]";
        if(separated.length%2==1)
            yang+=separated[separated.length-1];
        String j =null;
        if(YangToJava.containsValue(yang))
            for(String s:YangToJava.keySet())
                if(YangToJava.get(s).equals(yang))
                    j=s;
        if(j==null)
            return j;
        String[] java = j.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        j=new String();
        for(int i=0; i<java.length; i++){
            if(i%2==0)
                j+=java[i];
            else{
                j+="["+separated[i]+"]";
            }
        }
        if(y.endsWith("[]"))
            j+="[]";
        return j;
    }
    
        public Object getLists(Object actual, String remaining, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        String[] fs = remaining.split(Pattern.quote("/"));
        String fint = fs[0];
        if(fint.contains("[]")){
            //Siamo arrivati!
            fint = fint.substring(0, fint.length()-2);
            actual = actual.getClass().getField(fint).get(actual);
            return actual;
        }else{
            if(fint.contains("[")){
                //dobbiamo andare a prendere il valore giusto all'interno della lista
                String indice = fint.substring(fint.indexOf("[")+1, fint.length()-1);
                String listName = complete.substring(0, complete.length()-remaining.substring(fint.length()+1).length() -indice.length()-3) + "[]";                
                actual = actual.getClass().getField(fint.substring(0, fint.length()-indice.length()-2)).get(actual);
                Object item = null;
                if(List.class.isAssignableFrom(actual.getClass()))
                    item = getListItemWithIndex((List)actual, indice, listName);
                else if(Map.class.isAssignableFrom(actual.getClass()))
                    item = (((Map)actual).get(indice));
                return getLists(item, remaining.substring(fint.length()+1), complete);
            }else{
                //dobbiamo andare dentro l'oggetto
                actual = actual.getClass().getField(fint).get(actual);
                return getLists(actual, remaining.substring(fint.length()+1), complete);
            }
        }
    }

            //given a list, gets the element with the id specified
    private Object getListItemWithIndex(List list, String indice, String listName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String indexValue=null;
        String general = generalIndexes(listName)+"[]";
        if(lists.containsKey(listName)){
            indexValue = lists.get(listName);
        }
        if(indexValue!=null){
            for(Object obj:list){
                String i = obj.getClass().getField(indexValue).get(obj).toString();
                if(indice.equals(i))
                    return obj;
            }
        }
        return null;
    }

/*    private void findYangLeafs(YangTreeNode tree) {
        YANG_Body node = tree.getNode();
        //System.out.println(node);
        Vector<YangTreeNode> children = tree.getChilds();
        if(children.size()==0){
            //System.out.println("Is a leaf");
            YANG_Config config = node.getConfig();
            //System.out.println(config);
        }
        for(int i=0;i<children.size();i++){
            findYangLeafs(children.get(i));
        }
    }*/
    
    
    //Versione YIN (xml) non usata
    private void findYinLeafs(Element e, String prev){
        if(e.getTagName().equals("leaf")){
            //System.out.println(prev+"/"+e.getAttribute("name"));
            NodeList att = e.getChildNodes();
            for(int i=0;i<att.getLength();i++){
                if(att.item(i).getNodeName().equals("config")){
                    boolean c = (att.item(i).getAttributes().item(0).getNodeValue().equals("true"))?true:false;
                    //System.out.println("-+-config "+att.item(i).getAttributes().item(0).getNodeValue());
                    config.put(prev.substring(1)+"/"+e.getAttribute("name"), c);
                }
                //default
                if(!config.containsKey(prev+"/"+e.getAttribute("name")))
                    config.put(prev.substring(1)+"/"+e.getAttribute("name"), true);
            }
            //System.out.println("Lista config -- "+config);
            return;
        }
        Node n = (Node)e;
        NodeList children = n.getChildNodes();
        boolean list = e.getNodeName().equals("list");
        for(int i=0;i<children.getLength();i++)
            if(children.item(i).getNodeType()==Node.ELEMENT_NODE){
                String pref = prev+"/"+e.getAttribute("name");
                pref=(list)?pref+"[]":pref;
                findYinLeafs((Element)children.item(i), pref);
            }
    }
    
    
    //Versione "YIN" Json
    private void findYinLeafs(JsonNode y, String prev) {
        Iterator<Entry<String, JsonNode>> iter = y.fields();
        while(iter.hasNext()){
            Entry<String, JsonNode> value = iter.next();
            String fieldName = value.getKey();
            JsonNode valueNode = value.getValue();
            if(fieldName.equals("leaf")){
                //can be an array
                if(valueNode.isArray()){
                    Iterator<JsonNode> leafs = ((ArrayNode)valueNode).elements();
                    while(leafs.hasNext()){
                        ObjectNode child = (ObjectNode)leafs.next();
                        boolean conf;
                        if(child.get("config")!=null){
                            conf = child.get("config").get("@value").asBoolean();
                        }else{
                            conf = true;
                        }
                        //System.out.println("-+-config "+conf);
                        config.put(prev+"/"+child.get("@name").textValue(), conf);
                        
                        //check advertise attribute - prefix:advertise
                        Iterator<String> searchAdv = child.fieldNames();
                        String pref=null;
                        while(searchAdv.hasNext()){
                            String f = searchAdv.next();
                            if(f.endsWith(":advertise")){
                                pref = f.substring(0, f.length()-10);
                                break;
                            }
                        }
                        if(pref!=null){
                            //the advertise field is specified
                            String adv = child.get(pref+":advertise").get("@advertise").asText();
                            if(adv.equals("onchange")){
                                toListenPush.add(prev+"/"+child.get("@name").textValue());
                            }else if(adv.equals("periodic")){
                                if(child.has(pref+":period")){
                                    long p = child.get(pref+":period").get("@period").asLong();
                                    PeriodicVariableTask task = new PeriodicVariableTask(this, prev+"/"+child.get("@name").textValue());
                                    toListenTimer.add(task);
                                    timer.schedule(task, p, p);
                                }
                                //has to have!!
                            }else if(adv.equals("onthreshold")){
                                Object min = null;
                                Object max = null;
                                if(child.has(pref+":minthreshold")){
                                    min = child.get(pref+":minthreshold").get("@minthreshold").asDouble();
                                }
                                if(child.has(pref+":maxthreshold")){
                                    max = child.get(pref+":maxthreshold").get("@maxthreshold").asDouble();
                                }
                                if(min!=null || max!=null)
                                    toListenThreshold.put(prev+"/"+child.get("@name").textValue(), new Threshold(min, max));
                            }
                            //if never - nothing
                        }
                        //default:never
                    }
                }else{
                    //one single leaf 
                    boolean conf;
                    if(valueNode.get("config")!=null){
                        conf = valueNode.get("config").get("@value").asBoolean();
                    }else{
                        conf = true;
                    }
                    //System.out.println("-+-config "+conf);
                    config.put(prev+"/"+valueNode.get("@name").asText(), conf);
                    Iterator<String> searchAdv = valueNode.fieldNames();
                    String pref=null;
                    while(searchAdv.hasNext()){
                        String f = searchAdv.next();
                        if(f.endsWith(":advertise")){
                            pref = f.substring(0, f.length()-10);
                            break;
                        }
                    }
                    if(pref!=null){
                        //the advertise field is specified
                        String adv = valueNode.get(pref+":advertise").get("@advertise").asText();
                        if(adv.equals("onchange")){
                            toListenPush.add(prev+"/"+valueNode.get("@name").asText());
                        }else if(adv.equals("periodic")){
                            if(valueNode.has(pref+":period")){
                                long p = valueNode.get(pref+":period").get("@period").asLong();
                                PeriodicVariableTask task = new PeriodicVariableTask(this, prev+"/"+valueNode.get("@name").asText());
                                toListenTimer.add(task);
                                timer.schedule(task, p, p);
                            }
                            //has to have!!
                        }else if(adv.equals("onthreshold")){
                            Object min = null;
                            Object max = null;
                            if(valueNode.has(pref+":minthreshold")){
                                min = valueNode.get(pref+":minthreshold").get("@minthreshold").asDouble();
                            }
                            if(valueNode.has(pref+":maxthreshold")){
                                max = valueNode.get(pref+":maxthreshold").get("@maxthreshold").asDouble();
                            }
                            if(min!=null || max!=null)
                                toListenThreshold.put(prev+"/"+valueNode.get("@name").textValue(), new Threshold(min, max));
                        }
                        //if never - nothing
                    }
                    //default:never
                }
            }else{
                //traverse
                    if(valueNode.isArray()){
                        Iterator<JsonNode> objs = ((ArrayNode)valueNode).elements();
                        while(objs.hasNext()){
                            JsonNode next = objs.next();
                            if(next.has("@name")&&fieldName.equals("list"))
                                findYinLeafs(next, prev+"/"+next.get("@name").textValue()+"[]");
                            else if(next.has("@name"))
                                findYinLeafs(next, prev+"/"+next.get("@name").textValue());
                        }
                    }else{
                        if(valueNode.has("@name")&&fieldName.equals("list"))
                            findYinLeafs(valueNode, prev+"/"+valueNode.get("@name").textValue()+"[]");
                        else if(valueNode.has("@name"))
                            findYinLeafs(valueNode, prev+"/"+valueNode.get("@name").textValue());
                    }
            }
        }
    }


    private void checkThreshold(Map<String, Object> thr) {
        //values in stateNew
        for(String s: thr.keySet()){
            //if threshold -> publish
            boolean pub = false;
            String generalS = generalIndexes(s);
            String y = null;
            if(YangToJava.containsKey("root/"+generalS)){
                y = YangToJava.get("root/"+generalS);
                if(toListenThreshold.containsKey(y)){
                    if(toListenThreshold.get(y).MIN!=null){
                        if(toListenThreshold.get(y).MAX!=null){
                            if(((Number)thr.get(s)).doubleValue() > (Double)toListenThreshold.get(y).MIN && ((Number)thr.get(s)).doubleValue() < (Double)toListenThreshold.get(y).MAX)
                                pub = true;
                        }else if (((Number)thr.get(s)).doubleValue() > (Double)toListenThreshold.get(y).MIN){
                            pub = true;
                        }
                    }else{
                        if(((Number)thr.get(s)).doubleValue() < (Double)toListenThreshold.get(y).MAX)
                            pub = true;
                    }
                }
            }
            if(pub){
                if(!stateThreshold.containsKey(s) || !stateThreshold.get(s).equals(thr.get(s))){
                    NotifyMsg e = new NotifyMsg();
                    e.act = action.UPDATED;
                    e.var = trasformInPrint(s);
                    e.obj = thr.get(s);
                    stateThreshold.put(s, thr.get(s));
                    //System.out.println("---*ONTHRESHOLD");
                    //System.out.println((new Gson()).toJson(e));
                    log.info("*OnThreshold* "+(new Gson()).toJson(e));
                    cM.somethingChanged((new Gson()).toJson(e));
                }
            }else{
                if(stateThreshold.containsKey(s))
                    stateThreshold.remove(s);
            }
        }
    }
 
    public enum action{ADDED, UPDATED, REMOVED, NOCHANGES};
    public class NotifyMsg{
        public action act;
        public Object obj;
        public String var;
        public Date timestamp;

        public action getAct() {
            return act;
        }

        public void setAct(action act) {
            this.act = act;
        }

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public String getVar() {
            return var;
        }

        public void setVar(String var) {
            this.var = var;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }
    
    private class Threshold{
        public Object MAX;
        public Object MIN;
        
        public Threshold(Object MIN , Object MAX){
            this.MAX = MAX;
            this.MIN = MIN;
        }
    }
      
    
    //Task for periodic variables
    private class PeriodicVariableTask extends TimerTask{
        String var;
        StateListenerNew sl;
        
        public PeriodicVariableTask(StateListenerNew sl, String var){
            this.sl = sl;
            this.var = var;
            //System.out.println("COSTRUITO THREAD TIMER PER "+var);
        }
        
        public void run(){
//            sl.log.info("**Periodic Task of " + var+ " running**");
            Map<String, Object> listToSave = new HashMap<>();
            try{
                if(YangToJava.containsValue(var)){
                    String j = null;
                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(var)){
                            j = k;
                            break;
                        }
                    sl.saveValues(sl.root, j.substring(5), j.substring(5), listToSave);
                }
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(String s: listToSave.keySet()){
                NotifyMsg e = new NotifyMsg();
                e.act = action.NOCHANGES;
                e.obj = listToSave.get(s);
                e.var = sl.trasformInPrint(s);
                ////System.out.println("--*PERIODIC*-- " + System.currentTimeMillis());
                ////System.out.println((new Gson()).toJson(e));
                sl.cM.somethingChanged((new Gson()).toJson(e));
            }
        }
        
    }
    
//        private String allGeneralIndexes(String listName) {
//        String[] splitted = listName.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
//        String j=splitted[0];
//        String onlyLastOne = splitted[0];
//        String y=null;
//        if(splitted.length>1)
//            for(int i=1;i<splitted.length;i++){
//                if(i%2==0){
//                    //nome lista
//                    j+=splitted[i];
//                    onlyLastOne+=splitted[i];
//                }else{
//                    //chiave
//                    if(stateList.containsKey(onlyLastOne+"[]"))
//                        j+="["+stateList.get(onlyLastOne+"[]").idList+"]";
//                    onlyLastOne+=(i==splitted.length-1)?("["+stateList.get(onlyLastOne+"[]").idList+"]"):("["+splitted[i]+"]");
//                            
//                }
//            }
//        return j;
//    }
//        
//    //nullValuesToListen now exist? --> in state, removed form that list
//    //additions or removes in the lists --> added/deleted by state
//    //check if the variables in state have the same value or not
//    public void checkValue(){
//        try{
//            Gson gson = new Gson();
//            List<String> r = new ArrayList<>();
//            List<String> nulls = new ArrayList();
//            nulls.addAll(nullValuesToListen);
//            for(String s:nulls){
//                if(searchLeaf(root, s, s))
//                    r.add(s);
//            }
//            for(String s:r){
//                nullValuesToListen.remove(s);
//            }
//            List<String> copy = new ArrayList<>();
//            copy.addAll(stateList.keySet());
//            for(String lv:copy){
//                List<Object> act = (List)getLists(root, lv, lv);
//                List<NotifyMsg> wH = checkListChanges(lv, stateList.get(lv).List, act);  
//                
//                if(wH!=null){
//                    writeLock.lock();
//                    whatHappened.addAll(wH);
//                    writeLock.unlock();
//                }
//            }
//            List<String> copyState = new ArrayList();
//            if(state!=null)
//                copyState.addAll(state.keySet());
//            for(String s:copyState){
//                boolean c= getLeafValueChange(root, s, s);
//                if(!c){
//                    NotifyMsg e = new NotifyMsg();
//                    e.act=action.UPDATED;
//                    e.obj=state.get(s);
//                    e.var=s;
//                    writeLock.lock();
//                    whatHappened.add(e);
//                    writeLock.unlock();
//                }
//            }
//            List<NotifyMsg> wH = new ArrayList<>();
//            writeLock.lock();
//            wH.addAll(whatHappened);
//            whatHappened = new ArrayList<>();
//            writeLock.unlock();
//            if(wH!=null){
//                for(NotifyMsg e:wH){
//                    String toPrint = trasformInPrint(e.var);
//                    //System.out.println(e.act + " " + toPrint+" " + gson.toJson(e.obj));
//                }
//            }
//            
//            //System.out.println("state aggiornato: " + state);
//            //System.out.println("root "+(new Gson()).toJson(root));
//        } catch (NoSuchFieldException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }  
//    //returns true if the actual value and the old value are the same, false othercase
//    //recursive
//    private boolean getLeafValueChange(Object actual, String remaining, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
//        String[] fields = remaining.split(Pattern.quote("/"));
//        String finteresting = fields[0];
//        String fremaining = (fields.length>1)?remaining.substring(finteresting.length()+1):null;
//        boolean lista = false;
//        if(finteresting.contains("[")){
//            //devo andare a cercare il giusto oggetto dentro la lista
//            lista = true;
//            String indice = finteresting.substring(finteresting.indexOf("[")+1, finteresting.indexOf("]"));
//            String listName = complete.substring(0, complete.length()-fremaining.length()-indice.length()-3) + "[]";
//            actual = actual.getClass().getField(finteresting.substring(0, finteresting.length()-indice.length()-2)).get(actual);
//            if(actual==null){
//                if(state.get(complete)==null)
//                    return false;
//                else{
//                    //System.out.println("Rimossa lista");
//                    return true;
//                }
//            }
//            Object item = getListItemWithIndex((List)actual, indice, listName);
//            if(item==null){
//                //System.out.println("No items in list");
//                return false;
//            }
//            return getLeafValueChange(item, fremaining, complete);
//        }else{
//            if(fields.length>1){
//                actual = actual.getClass().getField(finteresting).get(actual);
//                if(actual==null){
//                    if(state.get(complete) == null)
//                        return false;
//                    else{
//                        //System.out.println("Removed obj " + finteresting);
//                        return true;
//                    }
//                }
//                return getLeafValueChange(actual, fremaining, complete);
//            }
//            else{
//                actual = actual.getClass().getField(finteresting).get(actual);
//                if(!state.containsKey(complete)){
//                    state.put(complete, actual);
//                    return false;
//                }
//                boolean rValue = state.get(complete).equals(actual);
//                if(!rValue)
//                    state.replace(complete, actual);
//                return rValue;
//            } 
//        }
//    }
//    
        //callable by the app
//    public void addNewListener(String name){
//        try {
//            searchLeaf(root, name, name);
//            toListen.add(name);
//        } catch (NoSuchFieldException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
////founds additions or removes of items in a list
//    public List<NotifyMsg> checkListChanges(String listName, List oldList, List newList) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
//        List<NotifyMsg> res = new ArrayList<>();
//        if(!stateList.containsKey(listName)){
//            //la lista è tutta nuova
//            String id = null; //prendere dal toListen
//            for(Object n:newList){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.ADDED;
//                e.obj = n;
//                e.var = listName;
//                res.add(e);
//                //ADD THE ELEMENT IN STATE
//                String savedInToListen = listName.substring(0, listName.length()-1) + id+"]";
//                List<String> save = new ArrayList<>();
//                for(String t:toListen){
//                    if(t.contains(savedInToListen) && t.substring(0, savedInToListen.length()).equals(savedInToListen))
//                        save.add(t);
//                }
//                for(String t:save){
//                    String fremaining = t.substring(savedInToListen.length());
//                    String idItem = savedInToListen.substring(0, savedInToListen.length()-id.length()-2)+n.getClass().getField(id).get(n).toString()+"]."+fremaining;
//                    searchLeaf(n, fremaining, idItem);
//                }
//            }
//            saveListstate(listName, id, newList);
//            return res;
//        }
//        String id = stateList.get(listName).idList;
//
//        if(oldList==null && newList==null){
//            return null;
//        }
//        if(oldList==null){
//            
//        }
//        if(newList==null){
//            for(Object old:oldList){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.REMOVED;
//                e.obj = old;
//                e.var = listName;
//                res.add(e);
//                //REMOVE THE ELEMENT IN STATE
//                List<String> remove = new ArrayList<>();
//                String identifier = listName.substring(0, listName.length()-1)+old.getClass().getField(id).get(old).toString()+"]";
//                for(String s:state.keySet()){
//                    if(s.contains(identifier) && s.substring(0, identifier.length()).equals(identifier))
//                        remove.add(s);
//                }
//                for(String s:remove)
//                    state.remove(s);
//            }
//            saveListstate(listName, id, newList);
//            return res;
//        }
//        List shadowCopy = new LinkedList();
//        shadowCopy.addAll(newList);
//        for(Object old:oldList){
//            String idValue = old.getClass().getField(id).get(old).toString();
//            boolean found = false;
//            for(Object n:shadowCopy){
//                String idValue2 = n.getClass().getField(id).get(n).toString();
//                if(idValue.equals(idValue2)){
//                    found = true;
//                    break;
//                }
//            }
//            if(found==false){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.REMOVED;
//                e.obj = old;
//                e.var = listName;
//                res.add(e);
//                //REMOVE THE ELEMENT IN STATE
//                List<String> remove = new ArrayList<>();
//                String identifier = listName.substring(0, listName.length()-1)+idValue+"]";
//                for(String s:state.keySet()){
//                    if(s.contains(identifier) && s.substring(0, identifier.length()).equals(identifier))
//                        remove.add(s);
//                }
//                for(String s:remove)
//                    state.remove(s);
//            }
//        }
//        for(Object n:shadowCopy){
//            String idValue = n.getClass().getField(id).get(n).toString();
//            boolean found = false;
//            for(Object old:oldList){
//                String idValue2 = old.getClass().getField(id).get(old).toString();
//                if(idValue.equals(idValue2)){
//                    found = true;
//                    break;
//                }
//            }
//            if(found==false){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.ADDED;
//                e.obj = n;
//                e.var = listName;
//                res.add(e);
//                //ADD THE ELEMENT IN STATE
//                String savedInToListen = allGeneralIndexes(listName);
//                savedInToListen +="["+id+"]";
//                List<String> save = new ArrayList<>();
//                for(String t:toListen){
//                    if(t.contains(savedInToListen) && t.substring(0, savedInToListen.length()).equals(savedInToListen))
//                        save.add(t);
//                }
//                for(String t:save){
//                    String fremaining = t.substring(savedInToListen.length()+1);
//                    //String idItem = savedInToListen.substring(0, savedInToListen.length()-id.length()-2)+idValue+"]."+fremaining;
//                    String idItem = listName.substring(0, listName.length()-1)+idValue+"]."+fremaining;
//                    searchLeaf(n, fremaining, idItem);
//                }
//            }
//            
//        }
//        if(!res.isEmpty())
//            saveListstate(listName, id, newList);    
//        return (res.isEmpty())?null:res;
//    }
//    //ricorsive method, if the object exists, puts the object to observe in state, and eventuals lists that founds in the stateList
//    //if still the object doesn't exists(or one of the "containers" puts the path to the object in the nullValuesToListen
//    private boolean searchLeaf(Object actual, String fields, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
//        String[] fs = fields.split(Pattern.quote("/"));
//        String finteresting = fs[0];
//        String fremaining = (fs.length>1)?fields.substring(finteresting.length()+1):null;
//        boolean lista = false;
//        String idLista=null;
//        Field f;
//        if(finteresting.contains("[")){
//            lista=true;
//            idLista = finteresting.substring(finteresting.indexOf("[")+1, finteresting.indexOf("]"));
//            finteresting = finteresting.substring(0, finteresting.indexOf("["));
//        }
//        f = actual.getClass().getField(finteresting);
//        actual = f.get(actual);
//        if(actual==null){
//            //salva temporaneamente: l'oggetto di interesse ancora non esiste
//            if(!nullValuesToListen.contains(complete))
//                nullValuesToListen.add(complete);
//            return false;
//        }
//        if(fs.length>1){
//            //calcola nuovo obj e nuovo fields
//            if(lista){
//                //searchLeaf in tutti gli elementi + controllo stato lista              
//                String idL = complete.substring(0,complete.length()-fremaining.length()-idLista.length()-2)+"]";
//                List ll = new ArrayList<>();
//                ll.addAll((List) actual);
//                boolean addedNw = saveListstate(idL, idLista, ll);
//                if(ll.size()!=0){
//                    boolean rValue = false;
//                    for(Object litem:ll){
//                        String idItem = searchLeafInList(litem, idLista);
//                        String idToPass = complete.substring(0,complete.length()-fremaining.length()-idLista.length()-2)+idItem+"]."+fremaining;
//                        boolean fitem = searchLeaf(litem, fremaining, idToPass);
//                        if(addedNw && fitem){
//                            NotifyMsg e = new NotifyMsg();
//                            e.act=action.ADDED;
//                            e.obj=litem;
//                            e.var = idToPass.substring(0, idToPass.length()-fremaining.length()-1);
//                            writeLock.lock();
//                            whatHappened.add(e);
//                            writeLock.unlock();
//                        }
//                        rValue=rValue||fitem;
//                    }
//                    return rValue;
//                }else
//                    return false;
//            }else
//                return searchLeaf(actual, fremaining, complete);
//        }else{
//            if(!state.containsKey(complete)){
//                NotifyMsg e = new NotifyMsg();
//                e.act=action.ADDED;
//                e.var=trasformInPrint(complete);
//                e.obj=actual;
//            }
//            state.put(complete, actual);
//            return true;
//        }
//    }
//    //copies the actual value of a list in the stateList
//    private boolean saveListstate(String key, String idLista, List ll) {
//        ////System.out.println("in saveListstate lists: " + stateList);
//        ListValues toRem = null;
//        if(ll==null){
//            stateList.remove(key);
//            //add in nullValuesToListen
//            String gen = allGeneralIndexes(key);
//            for(String s:toListen){
//                if(s.contains(gen) && s.substring(0, gen.length()).equals(gen)){
//                    try {
//                        boolean present = searchLeaf(root, s, s);
//                        if(!present)
//                            nullValuesToListen.add(s);
//                    } catch (NoSuchFieldException ex) {
//                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IllegalArgumentException ex) {
//                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IllegalAccessException ex) {
//                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                    
//            }
//            ////System.out.println("Dopo lista nulla in saveListstate "+stateList);
//            return true;
//        }
//        List nl = new LinkedList<>();
//        for(int i=0;i<ll.size();i++){
//            nl.add(ll.get(i));
//        }
//        if(stateList.containsKey(key)){
//            stateList.get(key).List = nl;
//            return false;}
//        stateList.put(key, new ListValues(idLista, nl));
//        ////System.out.println("Alla fine ho aggiunto qualcosa "+stateList);
//        return true;
//    }

}