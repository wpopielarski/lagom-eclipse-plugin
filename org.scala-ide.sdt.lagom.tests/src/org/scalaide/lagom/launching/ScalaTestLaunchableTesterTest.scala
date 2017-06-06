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

import org.junit.Test
import org.junit.Assert._
//import org.scalaide.lagom.launching.ScalaTestProject

class ScalaTestLaunchableTesterTest {
  
  import ScalaTestProject._
  
//  @Test
//  def testPackageTesterComTestPackage() {
//    val tester = new ScalaTestPackageTester()
//    
//    val comTestPackage = getPackageFragment("com.test")
//    assertTrue(tester.test(comTestPackage, "", Array.empty, null))
//  }
//  
//  @Test
//  def testPackageTesterSingleSpecFile() {
//    val tester = new ScalaTestPackageTester()
//    
//    val singleSpecFile = scalaCompilationUnit("com/test/SingleSpec.scala")
//    assertFalse(tester.test(singleSpecFile, "", Array.empty, null))
//    
//    singleSpecFile.getAllTypes.foreach { t => 
//      assertFalse(tester.test(t, "", Array.empty, null))      
//    }
//  }
//  
//  @Test
//  def testFileTesterComTestPackage() {
//    val tester = new ScalaTestFileTester()
//    
//    val comTestPackage = getPackageFragment("com.test")
//    assertFalse(tester.test(comTestPackage, "", Array.empty, null))
//  }
//  
//  @Test
//  def testFileTesterSingleSpecFile() {
//    val tester = new ScalaTestFileTester()
//    
//    val singleSpecFile = scalaCompilationUnit("com/test/SingleSpec.scala")
//    assertTrue(tester.test(singleSpecFile, "", Array.empty, null))
//    
//    singleSpecFile.getAllTypes.foreach { t => 
//      assertFalse(tester.test(t, "", Array.empty, null))      
//    }
//  }
//  
//  @Test
//  def testSuiteTesterComTestPackage() {
//    val tester = new ScalaTestSuiteTester()
//    
//    val comTestPackage = getPackageFragment("com.test")
//    assertFalse(tester.test(comTestPackage, "", Array.empty, null))
//  }
//  
//  @Test
//  def testSuiteTesterMultiSpecFile() {
//    val tester = new ScalaTestSuiteTester()
//    
//    val multiSpecFile = scalaCompilationUnit("com/test/MultiSpec.scala")
//    assertFalse(tester.test(multiSpecFile, "", Array.empty, null))
//    
//    multiSpecFile.getAllTypes.foreach { t => 
//      if (t.getFullyQualifiedName == "com.test.Fraction")
//        assertFalse(tester.test(t, "", Array.empty, null))
//      else
//        assertTrue(tester.test(t, "", Array.empty, null))
//    }
//  }
}