package org.scalaide.lagom.launching

import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group
import org.eclipse.debug.internal.ui.SWTFactory
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.ui.PlatformUI
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.jdt.internal.debug.ui.launcher.SharedJavaMainTab
import org.eclipse.swt.graphics.Image
import org.eclipse.jdt.ui.JavaUI
import org.eclipse.jdt.ui.ISharedImages
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.JavaCore
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.JavaModelException
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin
import org.eclipse.jdt.core.search.IJavaSearchScope
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine
import org.eclipse.jdt.core.IType
import java.lang.reflect.InvocationTargetException
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog
import org.eclipse.jface.window.Window
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.resources.IResource
import com.ibm.icu.text.MessageFormat
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants
import org.eclipse.core.runtime.CoreException
import org.scalaide.core.IScalaPlugin
import org.eclipse.core.runtime.IAdaptable
import org.scalaide.core.internal.jdt.model.ScalaSourceFile
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog
import org.eclipse.ui.dialogs.ResourceListSelectionDialog
import org.eclipse.core.resources.IFile
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.ui.dialogs.ElementListSelectionDialog
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.core.resources.IProject
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.TableColumn
import org.eclipse.swt.widgets.Table
import org.eclipse.jface.viewers.TableViewerColumn
import org.eclipse.swt.widgets.TableItem
import org.eclipse.swt.custom.TableEditor
import org.eclipse.swt.events.SelectionAdapter
import org.scalaide.core.IScalaProject

class ScalaTestMainTab extends SharedJavaMainTab {
  // UI widgets
  private var mainGroup: Group = null
  private var fSearchButton: Button = null
  private var fSuiteRadioButton: Button = null
  private var fFileRadioButton: Button = null
  private var fPackageRadioButton: Button = null
  private var fIncludeNestedCheckBox: Button = null
  
  private var testNamesGroup: Group = null
  private var fTestNamesTable: Table = null
  private var fTestNamesEditor: TableEditor = null
  
  def createControl(parent: Composite) {
    val comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH)
    comp.getLayout.asInstanceOf[GridLayout].verticalSpacing = 0
    createProjectEditor(comp)
    createVerticalSpacer(comp, 1)
    createMainTypeEditor(comp, "Suite Class")
    setControl(comp)
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB)
  }
  
  private def updateUI() {
    if (fSuiteRadioButton.getSelection) {
      fIncludeNestedCheckBox.setVisible(false)
      testNamesGroup.setVisible(true)
      mainGroup.setText("Suite Class")
    }
    else if (fFileRadioButton.getSelection) {
      fIncludeNestedCheckBox.setVisible(false)
      testNamesGroup.setVisible(false)
      mainGroup.setText("Suite File")
    }
    else {
      fIncludeNestedCheckBox.setVisible(true)
      testNamesGroup.setVisible(false)
      mainGroup.setText("Package Name")
    }
  }
  
  val typeChangeListener = new SelectionListener() {
    override def widgetSelected(e: SelectionEvent) {
      updateUI
      fMainText.setText("")
    }
    
    override def widgetDefaultSelected(e: SelectionEvent) {
      updateUI
    }
  }
  
  override protected def createMainTypeEditor(parent: Composite, text: String) {
    val typeGroup = SWTFactory.createGroup(parent, "Type", 3, 1, GridData.FILL_HORIZONTAL)
    fSuiteRadioButton = SWTFactory.createRadioButton(typeGroup, "Suite")
    fSuiteRadioButton.addSelectionListener(getDefaultListener)
    fSuiteRadioButton.addSelectionListener(typeChangeListener)
    fFileRadioButton = SWTFactory.createRadioButton(typeGroup, "File")
    fFileRadioButton.addSelectionListener(getDefaultListener)
    fFileRadioButton.addSelectionListener(typeChangeListener)
    fPackageRadioButton = SWTFactory.createRadioButton(typeGroup, "Package")
    fPackageRadioButton.addSelectionListener(getDefaultListener)
    fPackageRadioButton.addSelectionListener(typeChangeListener)
    
    mainGroup = SWTFactory.createGroup(parent, text, 2, 1, GridData.FILL_HORIZONTAL); 
    fMainText = SWTFactory.createSingleText(mainGroup, 1);
    fMainText.addModifyListener(new ModifyListener() {
      def modifyText(e: ModifyEvent) {
        updateLaunchConfigurationDialog()
      }
    })
    
    fSearchButton = createPushButton(mainGroup, LauncherMessages.AbstractJavaMainTab_2, null)
    fSearchButton.addSelectionListener(new SelectionListener() {
      def widgetDefaultSelected(e: SelectionEvent) {}
	  def widgetSelected(e: SelectionEvent) {
        handleSearchButtonSelected();
      }
    })
    createMainTypeExtensions(mainGroup)
    
    testNamesGroup = SWTFactory.createGroup(parent, "Test Names", 2, 1, GridData.FILL_BOTH)
    createTestNamesTable(testNamesGroup)
  }
  
  override protected def createMainTypeExtensions(parent: Composite) {
    fIncludeNestedCheckBox = SWTFactory.createCheckButton(parent, "Include nested", null, false, 1)
    fIncludeNestedCheckBox.addSelectionListener(getDefaultListener)
  }
  
  protected def createTestNamesTable(parent: Composite) {
    fTestNamesTable = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER)
    fTestNamesTable.setLayoutData(new GridData(GridData.FILL_BOTH))
    
    val column1 = new TableColumn(fTestNamesTable, SWT.NONE)
	column1.pack()
	
	fTestNamesEditor = new TableEditor(fTestNamesTable)
	fTestNamesEditor.horizontalAlignment = SWT.LEFT
	fTestNamesEditor.grabHorizontal = true
	fTestNamesEditor.minimumWidth = 300
	
	fTestNamesTable.addSelectionListener(new SelectionAdapter() {
	  override def widgetSelected(e: SelectionEvent) {
	    val oldEditor = fTestNamesEditor.getEditor
	    if (oldEditor != null)
	      oldEditor.dispose()
	      
	      val item = e.item.asInstanceOf[TableItem]
	      if (item == null)
	        return
	        
	      val newEditor = new Text(fTestNamesTable, SWT.NONE)
	      newEditor.setText(item.getText(0))
	      newEditor.addModifyListener(new ModifyListener() {
	        override def modifyText(me: ModifyEvent) {
	          val text = fTestNamesEditor.getEditor.asInstanceOf[Text]
	          fTestNamesEditor.getItem.setText(0, text.getText)
	          updateLaunchConfigurationDialog()
	        }
	      })
	      newEditor.selectAll()
	      newEditor.setFocus()
	      fTestNamesEditor.setEditor(newEditor, item, 0)
	  }
	})
	
	val comp = new Composite(parent, SWT.NONE)
	val gridLayout = new GridLayout()
	gridLayout.numColumns = 1
	comp.setLayout(gridLayout)
	comp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING))
	
	val addTestButton = createPushButton(comp, "Add", null)
	addTestButton.addSelectionListener(new SelectionListener() {
      def widgetDefaultSelected(e: SelectionEvent) { widgetSelected(e) }
	  def widgetSelected(e: SelectionEvent) {
        val item = new TableItem(fTestNamesTable, SWT.NONE)
        item.setText(Array[String]("[enter test name]"))
        updateLaunchConfigurationDialog()
      }
    })
	
	val removeTestButton = createPushButton(comp, "Remove", null)
	removeTestButton.addSelectionListener(new SelectionListener() {
	  def widgetDefaultSelected(e: SelectionEvent) { widgetSelected(e) }
	  def widgetSelected(e: SelectionEvent) {
	      val editor = fTestNamesEditor.getEditor
	      if (editor != null)
	        editor.dispose()
	      fTestNamesTable.remove(fTestNamesTable.getSelectionIndices())
	      fTestNamesTable.update()
	      updateLaunchConfigurationDialog()
	  }
	})
  }
  
  override def getImage = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
  
  def getName = LauncherMessages.JavaMainTab__Main_19
  
  override def getId = "scala.tools.eclipse.launching.scalaTestMainTab"; //$NON-NLS-1$
  
  protected def handleSearchButtonSelected() {
    val project = getJavaProject()
    var projects: Array[IJavaProject] = null
    if ((project == null) || !project.exists) {
      val model = JavaCore.create(ResourcesPlugin.getWorkspace.getRoot)
      if (model != null) {
        try {
          projects = model.getJavaProjects.filter {jp: IJavaProject => IScalaPlugin().getScalaProject(jp.getProject()).hasScalaNature }
        }
        catch { case e: JavaModelException => JDIDebugUIPlugin.log(e) }
      }
	}
    else {
      projects = Array(project)
    }
    if (projects == null) {
      projects = Array.empty
    }
    
    if (fSuiteRadioButton.getSelection) {
      val types: Array[IType] = 
        projects.flatMap { proj =>
          for (file <- IScalaPlugin().getScalaProject(proj.getProject).allSourceFiles;
            val ssf = ScalaSourceFile.createFromPath(file.getFullPath.toString); 
            if ssf.isDefined;
            iType <- ssf.get.getAllTypes;
            if LagomLaunchShortcut.isLagomApplicationLoader(iType)) yield iType
        }
    
      val mmsd = new DebugTypeSelectionDialog(getShell(), types, LauncherMessages.JavaMainTab_Choose_Main_Type_11) 
	  mmsd.setTitle("ScalaTest Suite Selection")
      if (mmsd.open() == Window.CANCEL) 
	    return
	
      val results = mmsd.getResult	
      val selectedType = results(0).asInstanceOf[IType]
      if (selectedType != null) {
        fMainText.setText(selectedType.getFullyQualifiedName())
        fProjText.setText(selectedType.getJavaProject().getElementName)
      }
    }
    else if (fFileRadioButton.getSelection) {
      val files = 
        projects.flatMap { proj =>
          for (file <- IScalaPlugin().getScalaProject(proj.getProject).allSourceFiles;
            val ssf = ScalaSourceFile.createFromPath(file.getFullPath.toString); 
            if ssf.isDefined && LagomLaunchShortcut.containsLagomLoaderClass(ssf.get)) yield file.asInstanceOf[IResource]
        }
      
      val fileSelectionDialog = new ResourceListSelectionDialog(getShell, files)
      fileSelectionDialog.setTitle("Scala Source File Selection")
      if (fileSelectionDialog.open() == Window.CANCEL) 
        return
        
      val results = fileSelectionDialog.getResult
      val selectedFile = results(0).asInstanceOf[IFile]
      if (selectedFile != null) {
        fMainText.setText(selectedFile.getFullPath.toPortableString)
        fProjText.setText(selectedFile.getProject.getName)
      }
    }
    else if (fPackageRadioButton.getSelection) {
      case class PackageOption(name: String, project: IJavaProject)
      val packageSet = 
        projects.flatMap { proj =>
          for (file <- IScalaPlugin().getScalaProject(proj.getProject).allSourceFiles;
            val ssf = ScalaSourceFile.createFromPath(file.getFullPath.toString); 
            if ssf.isDefined;
            iType <- ssf.get.getAllTypes;
            if LagomLaunchShortcut.isLagomApplicationLoader(iType)) yield PackageOption(iType.getPackageFragment.getElementName, iType.getJavaProject)
        }.toSet    
      
      val packageSelectionDialog = new ElementListSelectionDialog(getShell, new LabelProvider() { override def getText(element: Any) = element.asInstanceOf[PackageOption].name })
      packageSelectionDialog.setTitle("Package Selection")
      packageSelectionDialog.setMessage("Select a String (* = any string, ? = any char):")
      packageSelectionDialog.setElements(packageSet.toArray)
      if (packageSelectionDialog.open() == Window.CANCEL)
        return
        
      val results = packageSelectionDialog.getResult
      val selectedPackageOption = results(0).asInstanceOf[PackageOption]
      if (selectedPackageOption != null) {
        fMainText.setText(selectedPackageOption.name)
        fProjText.setText(selectedPackageOption.project.getElementName)
      }
    }
  }
  
  override def initializeFrom(config: ILaunchConfiguration) {
    super.initializeFrom(config)
    updateMainTypeFromConfig(config)
  }
  
  override protected def updateMainTypeFromConfig(config: ILaunchConfiguration) {
    super.updateMainTypeFromConfig(config)
    fSuiteRadioButton.setSelection(true)
    fFileRadioButton.setSelection(false)
    fPackageRadioButton.setSelection(false)
    fIncludeNestedCheckBox.setSelection(false)
    
    fTestNamesTable.removeAll()
    
    updateUI()
  }
  
  override def isValid(config: ILaunchConfiguration): Boolean = {
    setErrorMessage(null)
    setMessage(null)
    var name = fProjText.getText().trim()
    if (name.length > 0) {
      val workspace = ResourcesPlugin.getWorkspace
      val status = workspace.validateName(name, IResource.PROJECT)
      if (status.isOK) {
        val project= ResourcesPlugin.getWorkspace.getRoot.getProject(name)
        if (!project.exists()) {
          setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_20, Array(name)))
          return false
        }
	    if (!project.isOpen) {
          setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_21, Array(name))) 
          return false
        }
      }
      else {
        setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_19, Array(status.getMessage()))) 
        return false
      }
    }
    name = fMainText.getText.trim
    if (name.length == 0) {
      if (fSuiteRadioButton.getSelection)
        setErrorMessage("Suite Class cannot be empty.")
      else if (fFileRadioButton.getSelection)
        setErrorMessage("Suite File cannot be empty.")
     else
        setErrorMessage("Package Name cannot be empty.")
      return false
    }
    return true
  }
  
  def performApply(config: ILaunchConfigurationWorkingCopy) {
    val configMap = new java.util.HashMap[String, Any]()
    configMap.put(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText.trim)
    configMap.put(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fMainText.getText.trim) 
    
    config.setAttributes(configMap)
    mapResources(config)
  }
  
  def setDefaults(config: ILaunchConfigurationWorkingCopy) {
    val javaElement = getContext
    if (javaElement != null) 
      initializeJavaProject(javaElement, config)
    else 
      config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "")
    initializeMainTypeAndName(javaElement, config)
  }
}