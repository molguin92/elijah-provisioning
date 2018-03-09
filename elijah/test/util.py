
import os
import urllib2
import libvirt
import shutil
from elijah.provisioning.package import PackagingUtil


class Const(object):

    base_vm_cirros_url =\
        "https://storage.cmusatyalab.org/cloudlet-vm/cirros-0.3.4-x86_64-base.zip"
    overlay_url_cirros =\
        "https://storage.cmusatyalab.org/cloudlet-vm/cirros-overlay.zip"


class VMUtility(object):

    @staticmethod
    def get_VM_status(machine):
        machine_id = machine.ID()
        conn = libvirt.open("qemu:///session")
        for each_id in conn.listDomainsID():
            if each_id == machine_id:
                each_machine = conn.lookupByID(machine_id)
                vm_state, reason = each_machine.state(0)
                return vm_state
        return None

    @staticmethod
    def download_baseVM(url, download_file):
        req = urllib2.urlopen(url)
        CHUNK_SIZE = 1024*1024
        with open(download_file, 'wb') as fd:
            while True:
                chunk = req.read(CHUNK_SIZE)
                if not chunk:
                    break
                fd.write(chunk)

    @staticmethod
    def delete_basevm(base_path, base_hashvalue):
        if base_path is not None and base_hashvalue is not None:
            disk_path=base_path
            hash_value=base_hashvalue
            dbconn, matching_basevm = PackagingUtil._get_matching_basevm(disk_path)
            if matching_basevm:
                dbconn.del_item(matching_basevm)
            if matching_basevm:
                base_dir = os.path.dirname(base_path)
                shutil.rmtree(base_dir)

