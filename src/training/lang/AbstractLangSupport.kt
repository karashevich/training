/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.lang

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.wm.ToolWindowAnchor
import training.learn.exceptons.NoSdkException
import java.io.File

abstract class AbstractLangSupport : LangSupport {
    override val defaultProjectName: String
        get() = "LearnProject"

    override fun getProjectFilePath(projectName: String): String {
        return ProjectUtil.getBaseDir() + File.separator + projectName
    }

    override fun getToolWindowAnchor(): ToolWindowAnchor {
        return ToolWindowAnchor.LEFT
    }

    override fun getSdkForProject(project: Project): Sdk? {
        try {
            // Use no SDK if it's a valid for this language
            checkSdk(null, project)
            return null
        }
        catch (e: Throwable) {
        }
        
        return ApplicationManager.getApplication().runReadAction(ThrowableComputable<Sdk, NoSdkException> {
            val sdkOrNull = ProjectJdkTable.getInstance().allJdks.find {
                try {
                    checkSdk(it, project)
                    true
                } catch (e: Throwable) {
                    false
                }
            }
            sdkOrNull ?: throw NoSdkException()
        })
    }

    override fun applyProjectSdk(sdk: Sdk, project: Project) {
        CommandProcessor.getInstance().executeCommand(project, {
            ApplicationManager.getApplication().runWriteAction {

                val rootManager = ProjectRootManagerEx.getInstanceEx(project)
                rootManager.projectSdk = sdk
            }
        }, null, null)
    }

    override fun toString(): String = primaryLanguage
}