# GlChemistry
Engaging GL4 and porting legacy GL code

Infrastructure
----
	$ uname -r
	3.13.0-46-generic

	$ lsb_release -a
	No LSB modules are available.
	Distributor ID:	Ubuntu
	Description:	Ubuntu 14.04.2 LTS
	Release:	14.04
	Codename:	trusty
	
	$ lspci | grep -i radeon
	01:00.0 VGA compatible controller: Advanced Micro Devices, Inc. [AMD/ATI] Caicos [Radeon HD 6450/7450/8450 / R5 230 OEM]
	01:00.1 Audio device: Advanced Micro Devices, Inc. [AMD/ATI] Caicos HDMI Audio [Radeon HD 6400 Series]
	
	$ sudo lshw -c video
	  *-display               
       description: VGA compatible controller
       product: Caicos [Radeon HD 6450/7450/8450 / R5 230 OEM]
       vendor: Advanced Micro Devices, Inc. [AMD/ATI]
       physical id: 0
       bus info: pci@0000:01:00.0
       version: 00
       width: 64 bits
       clock: 33MHz
       capabilities: pm pciexpress msi vga_controller bus_master cap_list rom
       configuration: driver=fglrx_pci latency=0
       resources: irq:89 memory:d0000000-dfffffff memory:fea20000-fea3ffff ioport:e000(size=256) memory:fea00000-fea1ffff
	
	
	
	