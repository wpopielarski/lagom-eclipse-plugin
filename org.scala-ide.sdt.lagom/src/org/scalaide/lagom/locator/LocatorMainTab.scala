package org.scalaide.lagom.locator

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
import org.scalaide.lagom.cassandra.LagomCassandraConfiguration
import org.scalaide.lagom.kafka.LagomKafkaConfiguration

class LocatorMainTab extends AbstractMainTab {
  private var fPortText: Text = null
  private var fGatewayPortText: Text = null
  private var fCassandraPortText: Text = null
  private var fKafkaPortText: Text = null

  override def getId: String = "scalaide.lagom.locator.tabGroup"
  override def getName: String = "Lagom Service Locator"

  def createControl(parent: Composite): Unit = {
    val comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH)
    comp.getLayout.asInstanceOf[GridLayout].verticalSpacing = 0
    createProjectEditor(comp)
    createVerticalSpacer(comp, 1)
    createMainTypeEditor(comp, "Main Lagom Service Locator Parameters")
    setControl(comp)
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.LAUNCHER_CONFIGURATION)
  }

  private def createMainTypeEditor(parent: Composite, text: String): Unit = {
    val mainParamsGroup = SWTFactory.createGroup(parent, text, 2, 1, GridData.FILL_BOTH)
    createLines(mainParamsGroup)
    mainParamsGroup.pack()
  }

  protected def createLines(parent: Composite): Unit = {
    SWTFactory.createLabel(parent, "Service Locator Port", 1)
    fPortText = SWTFactory.createSingleText(parent, 1)
    fPortText.addModifyListener(new ModifyListener() {
      override def modifyText(me: ModifyEvent): Unit = {
        scheduleUpdateJob()
      }
    })
    SWTFactory.createLabel(parent, "Service Gateway Port", 1)
    fGatewayPortText = SWTFactory.createSingleText(parent, 1)
    fGatewayPortText.addModifyListener(new ModifyListener() {
      override def modifyText(me: ModifyEvent): Unit = {
        scheduleUpdateJob()
      }
    })
    SWTFactory.createLabel(parent, "Cassandra Port", 1)
    fCassandraPortText = SWTFactory.createSingleText(parent, 1)
    fCassandraPortText.addModifyListener(new ModifyListener() {
      override def modifyText(me: ModifyEvent): Unit = {
        scheduleUpdateJob()
      }
    })
    SWTFactory.createLabel(parent, "Kafka Port", 1)
    fKafkaPortText = SWTFactory.createSingleText(parent, 1)
    fKafkaPortText.addModifyListener(new ModifyListener() {
      override def modifyText(me: ModifyEvent): Unit = {
        scheduleUpdateJob()
      }
    })
  }

  private val image = Option(LagomImages.LAGOM_LOCATOR_SERVER.createImage)
  override def getImage = image.getOrElse(null)

  override def dispose: Unit = {
    image.foreach(_.dispose())
  }

  import LagomLocatorConfiguration._

  private def settingsValidator: Boolean =
    if (!fPortText.getText.trim.forall(Character.isDigit)) {
      setErrorMessage(s"Service Locator port must be a number.")
      false
    } else if (!fGatewayPortText.getText.trim.forall(Character.isDigit)) {
      setErrorMessage(s"Service Gateway port must be a number.")
      false
    } else if (!fCassandraPortText.getText.trim.forall(Character.isDigit)) {
      setErrorMessage(s"Cassandra port must be a number.")
      false
    } else if (!fKafkaPortText.getText.trim.forall(Character.isDigit)) {
      setErrorMessage(s"Kafka port must be a number.")
      false
    } else true

  override def isValid(config: ILaunchConfiguration): Boolean =
    isValid(settingsValidator)(config)

  override def initializeFrom(configuration: ILaunchConfiguration): Unit = {
    super.initializeFrom(configuration)
    fPortText.setText(configuration.getAttribute(LagomPort, LagomPortDefault))
    fGatewayPortText.setText(configuration.getAttribute(LagomGatewayPort, LagomGatewayPortDefault))
    fCassandraPortText.setText(configuration.getAttribute(LagomCassandraPort, LagomCassandraConfiguration.LagomPortDefault))
    fKafkaPortText.setText(configuration.getAttribute(LagomKafkaPort, LagomKafkaConfiguration.LagomPortDefault))
  }

  def performApply(config: ILaunchConfigurationWorkingCopy): Unit = {
    val configMap = new java.util.HashMap[String, Any]()
    configMap.put(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText.trim)
    configMap.put(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, LagomLocatorRunnerClass)
    configMap.put(LagomPort, fPortText.getText.trim)
    configMap.put(LagomGatewayPort, fGatewayPortText.getText.trim)
    configMap.put(LagomCassandraPort, fCassandraPortText.getText.trim)
    configMap.put(LagomKafkaPort, fKafkaPortText.getText.trim)
    config.setAttributes(configMap)
    mapResources(config)
  }

  def setDefaults(config: ILaunchConfigurationWorkingCopy): Unit = {
    setProjectName(config)
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, LagomLocatorName)
    config.setAttribute(LagomPort, LagomPortDefault)
    config.setAttribute(LagomGatewayPort, LagomGatewayPortDefault)
    config.setAttribute(LagomCassandraPort, LagomCassandraConfiguration.LagomPortDefault)
    config.setAttribute(LagomKafkaPort, LagomKafkaConfiguration.LagomPortDefault)
  }
}
