# Tanzu Customizer

Sample Code for customizing Tanzu on vSphere

Demonstrated Functionality:

- restart services on tanzu nodes (containerd for custom CAs)
- daemonset to configure nodes automatically (-> replaced nodes)
- addon for DNS management

If you want to build the project yourself, you have to 

## Prerequisites for node customization

Install Tanzu (Basic) on vSphere (tested without NSX-T) and deploy a workload cluster

If you want to build the project, checkout https://github.com/kubernetes-client/java (11.0.1-SNAPSHOT is referenced)

Get the ssh password for the Tanzu management cluster (ssh into vCenter, execute /usr/lib/vmware-wcp/decryptK8Pwd.py)

Get the kubeadm.conf from the management cluster (ssh into cluster master, copy /etc/kubernetes/admin.comf and change the server IP to the external IP)

Check connection as admin to the management cluster by testing

```bash
export KUBECONFIG=<yourpath>/admin.conf
kubectl get nodes -o wide
```
You should see the 3 master nodes with their IPs.

Open the firewall on each cluster node with [this command](k8s/firewall.txt)

Put the contents of admin.conf into [this file](k8s/base/config/admin.conf.template) and rename it to admin.conf

Switch to the namespace in which you have created your workload cluster

```bash
kubectl config set-context --current --namespace=<insert-namespace-name-here>
(or kubens <insert-namespace-name-here> - if you have the utiltiy kubens installed)
```

## Prerequisites for DNS updater (works only with ActiveDirectory/Microsoft DNS!)

enable the functionality in [env.properties](k8s/base/config/env.properties) (DNS_MANAGEMENT_ENABLED=true)

set the AD Server IP and credentials in the same file


Get the kubeconfig for your workload cluster, my cluster is named "workload-01"

```bash
kubectl get secret workload-01-kubeconfig -o jsonpath='{.data.value}' | base64 -d

```

Copy the contents in a file called cluster.conf in the directory /k8s/base/config.

Uncomment the line 24 in [kustomization.yaml](k8s/base/kustomization.yaml)

 




