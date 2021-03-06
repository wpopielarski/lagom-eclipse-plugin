package org.scalaide.lagom.cassandra

import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.internal.ui.SWTFactory
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants
import org.eclipse.pde.internal.ui.IHelpContextIds
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Text
import org.eclipse.ui.PlatformUI
import org.scalaide.lagom.AbstractMainTab
import org.scalaide.lagom.LagomImages

class CassandraMainTab extends AbstractMainTab {
  private var fPortText: Text = null
  private var fTimeoutText: Text = null

  override def getId: String = "scalaide.lagom.cassandra.tabGroup"
  override def getName: String = "Lagom Cassandra"

  def createControl(parent: Composite): Unit = {
    val comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH)
    comp.getLayout.asInstanceOf[GridLayout].verticalSpacing = 0
    createProjectEditor(comp)
    createVerticalSpacer(comp, 1)
    createMainTypeEditor(comp, "Main Lagom Cassandra Parameters")
    setControl(comp)
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.LAUNCHER_CONFIGURATION)
  }

  private def createMainTypeEditor(parent: Composite, text: String): Unit = {
    val mainParamsGroup = SWTFactory.createGroup(parent, text, 2, 1, GridData.FILL_BOTH)
    createLines(mainParamsGroup)
    mainParamsGroup.pack()
  }

  protected def createLines(parent: Composite): Unit = {
    SWTFactory.createLabel(parent, "Cassandra Server Port", 1)
    fPortText = SWTFactory.createSingleText(parent, 1)
    fPortText.addModifyListener(new ModifyListener() {
      override def modifyText(me: ModifyEvent): Unit = {
        scheduleUpdateJob()
      }
    })
    SWTFactory.createLabel(parent, "Cassandra Startup Timeout [ms]", 1)
    fTimeoutText = SWTFactory.createSingleText(parent, 1)
    fTimeoutText.addModifyListener(new ModifyListener() {
      override def modifyText(me: ModifyEvent): Unit = {
        scheduleUpdateJob()
      }
    })
  }

  private val image = Option(LagomImages.LAGOM_CASSANDRA_SERVER.createImage)
  override def getImage = image.getOrElse(null)

  override def dispose: Unit = {
    image.foreach(_.dispose())
  }

  import LagomCassandraConfiguration._

  private def settingsValidator: Boolean =
    if (!fPortText.getText.trim.forall(Character.isDigit)) {
      setErrorMessage(s"Cassandra port must be a number.")
      false
    } else if (!fTimeoutText.getText.trim.forall(Character.isDigit)) {
      setErrorMessage(s"Startup timeout must be a number.")
      false
    } else true

  override def isValid(config: ILaunchConfiguration): Boolean =
    isValid(settingsValidator)(config)

  override def initializeFrom(configuration: ILaunchConfiguration): Unit = {
    super.initializeFrom(configuration)
    fPortText.setText(configuration.getAttribute(LagomPort, LagomPortDefault))
    fTimeoutText.setText(configuration.getAttribute(LagomTimeout, LagomTimeoutDefault))
  }

  def performApply(config: ILaunchConfigurationWorkingCopy): Unit = {
    val configMap = new java.util.HashMap[String, Any]()
    configMap.put(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText.trim)
    configMap.put(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, LagomCassandraRunnerClass)
    configMap.put(LagomPort, fPortText.getText.trim)
    configMap.put(LagomTimeout, fTimeoutText.getText.trim)
    config.setAttributes(configMap)
    mapResources(config)
  }

  def setDefaults(config: ILaunchConfigurationWorkingCopy): Unit = {
    setProjectName(config)
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, LagomCassandraRunnerClass)
    config.setAttribute(LagomPort, LagomPortDefault)
    config.setAttribute(LagomTimeout, LagomTimeoutDefault)
  }
}
