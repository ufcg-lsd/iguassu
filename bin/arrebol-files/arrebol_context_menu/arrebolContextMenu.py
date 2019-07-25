import os, sys, glob, subprocess, urllib, json, time
from gi.repository import Nautilus, GObject, Gio
from Tkinter import *

class ColumnExtension(GObject.GObject, Nautilus.MenuProvider):

    ARREBOL_CLIENT_PATH="<TAG-VALUE-CLIENT-PATH>"

    def __init__(self):

        self.logActiveted = True
        self.log_info("LOG Init called")
        self.userName = None
        self.userPassword = None
        self.userPrivateKey = None
        self.root = Tk()
        self.user = StringVar()
        self.password = StringVar()
        self.privateKey = StringVar()
        self.msg = StringVar()
        self.selectedRadio = StringVar()
        self.credential = StringVar()

        self.passwordSelected = True

        self.msgError = StringVar()
        self.msgReturned = StringVar()
        self.jobId = StringVar()

        
        pass

    def selectCheckEvent(self, *args):
    	value = str(self.selectedRadio.get())
    	if value == "passRadio":
    		self.passwordSelected = True
    	else:
    		self.passwordSelected = False

    def authenticateUser(self, *args):
    	self.userName = str(self.user.get())
    	self.userPassword = str(self.password.get())
    	self.userPrivateKey = str(self.privateKey.get())
    	self.credential = None

    	if self.passwordSelected:
    		self.credential=self.userPassword
    	else:
    		self.credential=self.userPrivateKey

    	if not self.credential:
    		credType = "Password" if self.passwordSelected else "Private key"
    		self.log_error(credType+" must be informed")
    		self.msg.set(str(credType+" must be informed"))
    		return

    	self.user = None
    	self.password = None
    	self.privateKey = None
    	self.msg.set("")
    	self.root.destroy()
    	self.root = None

    def getUserCredentials(self):

		if self.root is None:
			self.root = Tk()
		self.root.title("Your Credentials")

		if self.user is None:
			self.user = StringVar()
		if self.password is None:
			self.password = StringVar()
		if self.privateKey is None:
			self.privateKey = StringVar()

		w = 400 # width for the Tk root
		h = 200 # height for the Tk root

		# get screen width and height
		ws = self.root.winfo_screenwidth() # width of the screen
		hs = self.root.winfo_screenheight() # height of the screen

		# calculate x and y coordinates for the Tk root window
		x = (ws/2) - (w/2)
		y = (hs/2) - (h/2)

		mainframe = Frame(self.root)
		mainframe.grid(column=0, row=0, sticky=(N, W, E, S))
		mainframe.columnconfigure(0, weight=1)
		mainframe.rowconfigure(0, weight=1)

		user_entry = Entry(mainframe, width=20, textvariable=self.user)
		user_entry.grid(column=2, row=1, sticky=(W, E))
		
		self.pass_entry = Entry(mainframe, width=20, textvariable=self.password, show="*")
		self.pass_entry.grid(column=2, row=2, sticky=(W, E))

		self.key_entry = Entry(mainframe, width=20, textvariable=self.privateKey)
		self.key_entry.grid(column=2, row=3, sticky=(W, E))

		self.labelUser = Label(mainframe, text="User").grid(column=1, row=1, sticky=W)
		self.labelPass = Label(mainframe, text="Password").grid(column=1, row=2, sticky=W)
		self.labelKey = Label(mainframe, text="Private Key File").grid(column=1, row=3, sticky=W)
		self.labelMsg = Label(mainframe, textvariable=self.msg, fg='red').grid(column=2, row=4, sticky=W)

		passRadio = Radiobutton(mainframe, text='Use password', variable=self.selectedRadio, value='passRadio', command=self.selectCheckEvent).grid(column=1, row=4, sticky=W)
		keyRadio = Radiobutton(mainframe, text='Use private key file', variable=self.selectedRadio, value='keyRadio', command=self.selectCheckEvent).grid(column=1, row=5, sticky=W)

		Button(mainframe, text="Authenticate", command=self.authenticateUser).grid(column=2, row=5, sticky=W)

		for child in mainframe.winfo_children(): child.grid_configure(padx=5, pady=5)

		user_entry.focus()
		#root.bind('<Return>', calculate)

		# set the dimensions of the screen 
		# and where it is placed
		self.root.geometry('%dx%d+%d+%d' % (w, h, x, y))
		self.root.mainloop()

    def showReturn(self, msg):

		try:
			showReturnRoot = Tk()
			showReturnRoot.title("Execution Result")
			
			label = Message( showReturnRoot, text=str(msg), relief=RAISED, width=250)
			label.pack()

			w = 300
			h = 100
			ws = showReturnRoot.winfo_screenwidth() # width of the screen
			hs = showReturnRoot.winfo_screenheight() # height of the screen

			x = (ws/2) - (w/2)
			y = (hs/2) - (h/2)

			showReturnRoot.geometry('%dx%d+%d+%d' % (w, h, x, y))
			showReturnRoot.mainloop()

		except AttributeError, e:
			self.log_error(str(e))
		except:
			self.log_error(str(sys.exc_info()[0]))

    def menu_activate_execute(self, menu, selectedFile):

		self.getUserCredentials()

		fileCompleteName = str(selectedFile.get_uri())[7:]
		command = "bash "+str(self.ARREBOL_CLIENT_PATH)+" POST "+str(fileCompleteName)+" --userId "+str(self.userName)
		out, err = subprocess.Popen("echo '"+str(self.credential)+"' | "+str(command), stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell = True).communicate()
		
		try:
			
			outStr = str(out).strip()
			errStr = str(err).strip()
			self.log_info("Out process returned "+str(outStr))
			self.log_info("Err process returned "+str(errStr))

			if not outStr:
				if not errStr:
					self.msgError = "An error ocurred when tried to connect to Arrebol. Maybe Arrebol Server is busy or down. Please try again later."
				else:
					self.msgError = errStr
			else:
				if outStr.startswith("Password:"):
					outStr = outStr[9:]
					try:
						responseJson = json.loads(str(outStr))
						self.msgError = str(responseJson["reasonPhrase"])+"\n"+str(responseJson["description"])
					except ValueError, e:
						#Is not a json, should be the ID of the job
						self.msgError = str(outStr)
				else:
					self.jobId = str(outStr)
				
			if not self.msgError:
				self.log_info("Job successfully sent")
				self.add_new_emblem("emblem-arrebol-exec-32", fileCompleteName)
				self.msgReturned = self.jobId
			else:
				self.log_error("Job sent failed")
				self.msgReturned = self.msgError

			self.showReturn(self.msgReturned)

		except AttributeError, e:
			self.log_error(str(e))
		except:
			self.log_error(str(sys.exc_info()[0]))

    def menu_activate_monitor(self, menu, selectedFile):
		os.system("echo 'Execution monitor' >> /tmp/log_menu.log")

		fileCompleteName = str(selectedFile.get_uri())[7:]

		self.add_new_emblem("emblem-arrebol-icon-32", fileCompleteName)

    def get_file_items(self, window, files):

		jdfExtension = ".jdf"
		#os.system("echo 'Context Menu Activated' > /tmp/log_menu.log")
		#os.system("echo '"+str(Gio.FileType.REGULAR)+"' > /tmp/log_menu.log")

		for selectedFile in files:
			fileType = selectedFile.get_file_type()
			#os.system("echo 'Files received "+str(file_type)+" ' >> /tmp/log_menu.log")
			#os.system("echo 'Name "+str(selectedFile.get_name())+" ' >> /tmp/log_menu.log")
			filename, file_extension = os.path.splitext(str(selectedFile.get_name()))
			#os.system("echo 'filename "+str(filename)+" ' >> /tmp/log_menu.log")
			#os.system("echo 'file_extension "+str(file_extension)+" ' >> /tmp/log_menu.log")

			if str(fileType) == str(Gio.FileType.REGULAR) and file_extension == jdfExtension:
				submenu = Nautilus.Menu()

				sub_menuitem_execute = Nautilus.MenuItem(name='ArrebolMenuProvider::execute_jdf', 
                                         label='Execute JDF', 
                                         tip='',
                                         icon='')
				sub_menuitem_execute.connect('activate', self.menu_activate_execute, selectedFile)
				submenu.append_item(sub_menuitem_execute)

				#sub_menuitem_monitor = Nautilus.MenuItem(name='ArrebolMenuProvider::monitor_jdf', 
                #                         label='Monitor JDF', 
                #                         tip='',
                #                         icon='')
				#sub_menuitem_monitor.connect('activate', self.menu_activate_monitor, selectedFile)
				#submenu.append_item(sub_menuitem_monitor)

				top_menuitem = Nautilus.MenuItem(name='ArrebolMenuProvider::Arrebol', 
	                                         label='Arrebol', 
	                                         tip='',
	                                         icon='')
		
				top_menuitem.set_submenu(submenu)

				return top_menuitem,
			else:
				return

		#for menuFile in files:        # Second Example
		#  self.fileName=fileName+" "+menuFile.get_file_type() 	

		#if len(files) != 1 or files[0].get_mime_type() != 'text/plain': return
		
    def add_new_emblem(self, emblem, fileCompleteName):
		os.system("gvfs-set-attribute -t unset '"+fileCompleteName+"' metadata::emblems") # Removes previous emblems.
		os.system("gvfs-set-attribute -t stringv '"+fileCompleteName+"' metadata::emblems $(gvfs-info '"
			+fileCompleteName+"' | grep \"metadata::emblems:\" | sed s/\metadata::emblems:// | tr -d [,]) "+str(emblem))
		os.system("touch "+fileCompleteName); # Touch the file to update informations about emblems.

    def log_on_file(self, msg, logType):
		if self.logActiveted:
			localtime = time.asctime(time.localtime(time.time()))
			os.system("echo '"+str(localtime)+" - "+str(logType)+" "+str(msg)+"' >> /tmp/log_menu.log")
    def log_error(self, msg):
		self.log_on_file(msg, "ERROR")
    def log_info(self, msg):
		self.log_on_file(msg, "INFO")