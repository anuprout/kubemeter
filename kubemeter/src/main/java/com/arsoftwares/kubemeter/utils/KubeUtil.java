package com.arsoftwares.kubemeter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Copy;
import io.kubernetes.client.Exec;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.ExtensionsV1beta1Deployment;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.models.V1StatusBuilder;
import io.kubernetes.client.models.V1beta1DaemonSet;
import io.kubernetes.client.models.V1beta1DaemonSetList;
import io.kubernetes.client.util.Config;



/**
 * @author anuprout
 *
 */

public class KubeUtil {
	private ApiClient client = null;
	
	
	public KubeUtil(File kubeConfigFile) throws Exception{
		this.client = Config.fromConfig(new FileInputStream(kubeConfigFile));
		client.getHttpClient().setWriteTimeout(120, TimeUnit.SECONDS);
		client.getHttpClient().setReadTimeout(120, TimeUnit.SECONDS);
		
	}

	
	
	/**
	 * @return
	 * @throws Exception
	 */
	public  List<String> getNameSpaces() throws Exception{
		CoreV1Api api = new CoreV1Api(client); 
		V1NamespaceList nameSpaceList = api.listNamespace(null, null, null, null, null, null, null, null, null);
		List<String> nameSpaces = new ArrayList<String>();
		for (V1Namespace item : nameSpaceList.getItems()) {
            nameSpaces.add(item.getMetadata().getName());
        }
		
		return nameSpaces;
	}
	
	/**
	 * @param nameSpace
	 * @return
	 * @throws Exception
	 */
	public  List<String> getServicesForNameSpace(String nameSpace) throws Exception{
		List<String> servicesList = new ArrayList<String>();
		
		CoreV1Api api = new CoreV1Api(client);
		V1ServiceList listNamedspacedServices = api.listNamespacedService(nameSpace, null, null, null, null, null, null, null, null, null);
		
		for (V1Service item : listNamedspacedServices.getItems()) {
			servicesList.add(item.getMetadata().getName());
        }
		
		return servicesList;
	}
	
	/**
	 * @param nameSpace
	 * @return
	 * @throws Exception
	 */
	public  List<String> getPodsForNameSpace(String nameSpace) throws Exception{
		List<String> podsList = new ArrayList<String>();
		
		CoreV1Api api = new CoreV1Api(client); 
		V1PodList listNamedspacedPods = api.listNamespacedPod(nameSpace, null, null, null, null, null, null, null, null, null);
		
		for (V1Pod item : listNamedspacedPods.getItems()) {
			podsList.add(item.getMetadata().getName()); 
			
        }
		
		
		return podsList;
		
		
	}
	
	/**
	 * @param nameSpace
	 * @param labelName
	 * @param labelValue
	 * @return
	 * @throws Exception
	 */
	public  V1PodList getPodsForNameSpaceWithLabel(String nameSpace, String labelName, String labelValue) throws Exception{
		
		CoreV1Api api = new CoreV1Api(client); 
		V1PodList listNamedspacedPods = api.listNamespacedPod(nameSpace, null, null, null, null, labelName+"="+labelValue, null, null, null, null);
		
		/*for (V1Pod item : listNamedspacedPods.getItems()) {
			podsList.add(item.getMetadata().getName()); 
			System.out.println(item);
        }*/
		
		
		return listNamedspacedPods;
		
		
	}
	
	/**
	 * @param nameSpace
	 * @return
	 * @throws Exception
	 */
	public  List<String> getAppsForNameSpace(String nameSpace) throws Exception{
		List<String> appsList = new ArrayList<String>();
		
		CoreV1Api api = new CoreV1Api(client); 
		V1PodList listNamedspacedPods = api.listNamespacedPod(nameSpace, null, null, null, null, null, null, null, null, null);
		
		for (V1Pod item : listNamedspacedPods.getItems()) {
			String app = item.getMetadata().getLabels().get("app");
			if(app != null) appsList.add(app); 
			
        }
		
		
		return appsList;
		
		
	}
	
	/**
	 * @param nameSpace
	 * @return
	 * @throws Exception
	 */
	public  List<String> getDemonsForNameSpace(String nameSpace) throws Exception{
		List<String> demonsList = new ArrayList<String>();
		
		ExtensionsV1beta1Api extApi =  new ExtensionsV1beta1Api(client);
		V1beta1DaemonSetList listNamespacedDaemonSet = extApi.listNamespacedDaemonSet(nameSpace, null, null, null, null, null, null, null, null, null);
		
		for(V1beta1DaemonSet item: listNamespacedDaemonSet.getItems()) {
			demonsList.add(item.getMetadata().getName());
			//System.out.println(item.getMetadata().getName());
		}
		
		return demonsList;
	}
	
	/**
	 * @param nameSpace
	 * @param podName
	 * @throws Exception
	 */
	public void portForwardForPOD(String nameSpace, String podName) throws Exception{
		CoreV1Api api = new CoreV1Api(client); 
		try {
			System.out.println(api.connectPostNamespacedPodPortforwardWithHttpInfo(podName, nameSpace,9010));
		} catch (ApiException e) {
			System.out.println(e.getResponseBody());
			e.printStackTrace();
		}
	}
	
	/**
	 * @param namespace
	 * @param deploymentYaml
	 * @return
	 * @throws Exception
	 */
	public ExtensionsV1beta1Deployment createDeployment(String namespace, String deploymentYaml) throws Exception {
		ExtensionsV1beta1Api api = new ExtensionsV1beta1Api(client);
		
		Yaml yaml = new Yaml();
		ExtensionsV1beta1Deployment body = yaml.loadAs(new FileInputStream(deploymentYaml), ExtensionsV1beta1Deployment.class);
		
		return api.createNamespacedDeployment(namespace, body,true, "true","All");
	}
	
	/**
	 * @param namespace
	 * @param podYaml
	 * @return
	 * @throws Exception
	 */
	public V1Pod createPod(String namespace, String podYaml,String podName, String testID) throws Exception {
		CoreV1Api api = new CoreV1Api(client); 
		
		podYaml = podYaml.replace("<PODNAME>", podName);
		podYaml = podYaml.replace("<TESTID>", testID);
		
		Representer representer = new Representer();
		representer.getPropertyUtils().setSkipMissingProperties(true);
		Yaml yaml = new Yaml(new Constructor(V1Pod.class),representer);
		V1Pod body = yaml.load(podYaml);
		
		body.getMetadata().setName(podName);
		body.getMetadata().getLabels().put("testid", testID);
		
				
		return api.createNamespacedPod(namespace, body, true, "false", null);
	}
	
	
	
	public V1Pod getPodByName(String namespace, String podName) throws Exception{
		CoreV1Api api = new CoreV1Api(client);
		return api.readNamespacedPod(
				podName, 
				namespace, 
				"false", 
				false, 
				false);
		
	}
	
	public V1PodList searchPodsByLabel(String namespace, Map<String,String> labels) throws Exception {
		
		//build selector string
		String labelSelector =  labels.keySet().stream()
	      .map(key -> key + "=" + labels.get(key))
	      .collect(Collectors.joining(", ", "", ""));
		
		CoreV1Api api = new CoreV1Api(client);
				
		return api.listNamespacedPod(
				namespace, 
				true, 
				"false", 
				null, 
				null, 
				labelSelector, 
				null, 
				null, 
				30, //time out 30 secs
				null
				);
		
		
	}
	
	public V1Status deletePOD(String namespace, String podName) throws Exception {
		CoreV1Api api = new CoreV1Api(client);
		V1Status deleteStatus = new V1StatusBuilder()
				.build()
				.apiVersion("v1")
				.kind("Status")
				;
		
		
		Call call = api.deleteNamespacedPodCall(podName, 
				namespace, 
				"false", 
				null, 
				null, 
				0, 
				true, 
				null,
				null,
				null);

		Response res = call.execute();
		deleteStatus.setCode(res.code());
		deleteStatus.setMessage(res.message());
		deleteStatus.setStatus(res.isSuccessful()?"Success":"Failure");
		
		//return client.deserialize(res, new TypeToken<V1Pod>(){}.getType());
			
		return deleteStatus;
	}
	
	/**
	 * @param namespaceName
	 * @return
	 * @throws Exception
	 */
	public V1Namespace createNamespace(String namespaceName) throws Exception {
		CoreV1Api api = new CoreV1Api(client);
		
		V1ObjectMeta metadataBody = new V1ObjectMeta();
		metadataBody.setName(namespaceName);
		
		V1Namespace namespaceBody = new V1Namespace();
		namespaceBody.setMetadata(metadataBody);
			
		return api.createNamespace(namespaceBody, true, "true", "All");
	}
	
	/**
	 * @param namespace
	 * @param configMapYaml
	 * @return
	 * @throws Exception
	 */
	public V1ConfigMap createConfigMap(String namespace, String configMapYaml) throws Exception {
		CoreV1Api api = new CoreV1Api(client);
			
			Yaml yaml = new Yaml();
			V1ConfigMap body = yaml.loadAs(new FileInputStream(configMapYaml), V1ConfigMap.class);
			
			return api.createNamespacedConfigMap(namespace, body, false, "true", "All");
		
	}
	
	public void copyMultipartFilesToPod(String namespace, String podName, String podDir, MultipartFile[] fileIns) throws Exception {
		if(fileIns.length == 0) return;
		
		Exec exec = new Exec(client);
		
		final Process proc =
                exec.exec(
                        namespace,
                        podName,
                        //new String[]{"tee" ,podFilePath},
                        new String[] {"tar", "-xmf", "-", "-C", podDir+"/"},
                        true,
                        false);
		
		OutputStream podOut = null;
        try {
            podOut = proc.getOutputStream();
            
            TarArchiveOutputStream taos = new TarArchiveOutputStream(podOut);
	        // TAR has an 8 gig file limit by default, this gets around that
	        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
	        // TAR originally didn't support long file names, so enable the support for it
	        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
	        taos.setAddPaxHeadersForNonAsciiNames(true);
            
	        for(MultipartFile fileIn:fileIns) {
	        	TarArchiveEntry tarEntry = new TarArchiveEntry(fileIn.getOriginalFilename());
	        	tarEntry.setSize(fileIn.getSize());
	            taos.putArchiveEntry(tarEntry);
	            //IOUtils.copy(fileIn.getInputStream(), taos);
	            taos.write(fileIn.getBytes());
	            taos.closeArchiveEntry();
	            
	        }
	        
           //podOut.flush(); 
           //podOut.close(); 
           
	        taos.close();
                       
        } catch (Exception ex) {
            throw ex;
        }
        finally {
        	proc.destroy(); //need to check on this since destroying the proc causing an error 
        	
        }
        
        	
	}
	
	public void copyMultipartFileToPod(String namespace, String podName, String podDir, MultipartFile fileIn) throws Exception {
		
		Exec exec = new Exec(client);
		
		final Process proc =
                exec.exec(
                        namespace,
                        podName,
                        //new String[]{"tee" ,podFilePath},
                        new String[] {"tar", "-xmf", "-", "-C", podDir+"/"},
                        true,
                        false);
		
		OutputStream podOut = null;
        try {
            podOut = proc.getOutputStream();
            
            TarArchiveOutputStream taos = new TarArchiveOutputStream(podOut);
	        // TAR has an 8 gig file limit by default, this gets around that
	        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
	        // TAR originally didn't support long file names, so enable the support for it
	        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
	        taos.setAddPaxHeadersForNonAsciiNames(true);
            
	        TarArchiveEntry tarEntry = new TarArchiveEntry(fileIn.getOriginalFilename());
        	tarEntry.setSize(fileIn.getSize());
            taos.putArchiveEntry(tarEntry);
            //IOUtils.copy(fileIn.getInputStream(), taos);
            taos.write(fileIn.getBytes());
            taos.closeArchiveEntry();
	        
            //podOut.flush(); 
            podOut.close(); 
            taos.close();
                       
        } catch (Exception ex) {
            throw ex;
        }
        finally {
        	proc.destroy(); //need to check on this since destroying the proc causing an error 
        	
        }
        
        	
	}
	
	public void copyFileToPod(String namespace, String podName, byte[] bytesIn, String podFilePath) throws Exception {
		Exec exec = new Exec(client);  
		
		final Process proc =
                exec.exec(
                        namespace,
                        podName,
                       // new String[]{"tee" ,podFilePath},
                        new String[] {"tar", "-xmf", "-", "-C", "/"},
                        true,
                        false);
		
		OutputStream podOut = null; 
        try {
            podOut = proc.getOutputStream();
            
            TarArchiveOutputStream taos = new TarArchiveOutputStream(podOut);
	        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
	        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
	        taos.setAddPaxHeadersForNonAsciiNames(true);
            
	        TarArchiveEntry tarEntry = new TarArchiveEntry(podFilePath);
	        tarEntry.setSize(bytesIn.length);
            taos.putArchiveEntry(tarEntry);
            
           taos.write(bytesIn);
            taos.closeArchiveEntry();
            
          taos.close();
          podOut.close();
            
        } catch (Exception ex) {
            throw ex;
        }
        finally {
        	proc.destroy(); //need to destroy it in order to close the kubernetes API connection
        	
        }
	}
	
	public InputStream copyFileFromPod(String namespace, String podName, String srcPath) throws Exception {
		Copy copy = new Copy(client);
		
		return copy.copyFileFromPod(namespace, podName, srcPath);
	}

	
	public void executeCommandInPOD(String namespace, String podName,String[] command) throws Exception{
		Exec exec = new Exec(client);  
		final Process proc =
                exec.exec(
                        namespace,
                        podName,
                        command,
                        false,
                        false);
	
		//proc.destroy();
		
	}
	
	public void createConfigMap(String namespace) throws Exception{
		
		CoreV1Api api = new CoreV1Api(client);
		V1ConfigMap cmap = new V1ConfigMap();
		cmap.setData(data);
		
	}
	
	
}
