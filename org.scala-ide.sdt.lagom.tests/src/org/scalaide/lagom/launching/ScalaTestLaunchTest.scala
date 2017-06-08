/*
 * SCALA LICENSE
 *
 * Copyright (C) 2011-2012 Artima, Inc. All rights reserved.
 *
 * This software was developed by Artima, Inc.
 *
 * Permission to use, copy, modify, and distribute this software in source
 * or binary form for any purpose with or without fee is hereby granted,
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the EPFL nor the names of its contributors
 *    may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package org.scalaide.lagom.launching

import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchManager
import org.junit.Ignore
import org.junit.Test
import ScalaTestProject.file
import org.scalaide.lagom.launching.ScalaTestProject

class ScalaTestLaunchTest {

  // Tests marked with @Ignore requires jar file for specs1 and scalachecks wrapper runner,
  // which is not in any public maven repo yet.  We could enable them back
  // when they are in public maven repo.

  import ScalaTestProject._

  private def launch(launchName: String) {
    val launchConfig = DebugPlugin.getDefault.getLaunchManager.getLaunchConfiguration(file(launchName + ".launch"))
    launchConfig.launch(ILaunchManager.RUN_MODE, null)
  }

  @Test
  def testLaunchComTestPackage() {
    launch("com.test")
  }

  @Test
  def testLaunchSingleSpecFile() {
    launch("SingleSpec.scala")
  }

  @Test
  def testLaunchMultiSpecFile() {
    launch("MultiSpec.scala")
  }

  @Test
  def testLaunchSingleSpec() {
    launch("SingleSpec")
  }

  @Test
  def testLaunchStackSpec2() {
    launch("StackSpec2")
  }

  @Test
  def testLaunchTestingFreeSpec() {
    launch("TestingFreeSpec")
  }

  @Test
  def testLaunchTestingFunSuite() {
    launch("TestingFunSuite")
  }

  @Test
  def testLaunchConfigAStackshouldtastelikepeanutbutter() {
    launch("AStackshouldtastelikepeanutbutter")
  }

  @Test
  def testLaunchConfigAStackwhenemptyshouldcomplainonpop() {
    launch("AStackwhenemptyshouldcomplainonpop")
  }

  @Test
  def testLaunchConfigAStackwhenfull() {
    launch("AStackwhenfull")
  }

  @Test
  def testLaunchConfigAStackwheneveritisemptycertainlyoughttocomplainonpeek() {
    launch("AStackwheneveritisemptycertainlyoughttocomplainonpeek")
  }

  @Test
  def testLaunchConfigAStackwheneveritisempty() {
    launch("AStackwheneveritisempty")
  }

  @Test
  def testLaunchConfigAStack() {
    launch("AStack")
  }

  @Test
  def `testLaunchConfigcom.test.TestingFunSuite-'test2'`() {
    launch("com.test.TestingFunSuite-'test2'")
  }

  @Ignore
  def testLaunchExampleSpec1File() {
    launch("ExampleSpec1.scala")
  }

  @Ignore
  def testLaunchExampleSpec1() {
    launch("ExampleSpec1")
  }

  @Ignore
  def testLaunchConfigMysystem() {
    launch("Mysystem")
  }

  @Ignore
  def testLaunchConfigMysystemalsocanprovidesadvancedfeature1() {
    launch("Mysystemalsocanprovidesadvancedfeature1")
  }

  @Ignore
  def testLaunchStringSpecificationFile() {
    launch("StringSpecification.scala")
  }

  @Ignore
  def testLaunchSpringSpecification() {
    launch("StringSpecification")
  }

  @Ignore
  def `testLaunchConfigcom.test.StringSpecification-'substring1'`() {
    launch("com.test.StringSpecification-'substring1'")
  }
}