package com.mm.router_compiler

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.mm.router_compiler.util.ServicesFiles
import com.mm.router_compiler.util.TypeUtils
import java.io.IOException
import java.util.SortedSet
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation


/**
 * Date : 2023/5/16
 */
abstract class BaseAbstractProcessor : AbstractProcessor() {
    lateinit var mFiler: Filer  //文件相关的辅助类
    lateinit var mElements: Elements   //元素相关的辅助类
    lateinit var mMessager: Messager     //日志相关的辅助类
    lateinit var mTypes: Types
    lateinit var mTypeUtils: TypeUtils
    var moduleName: String = ""

    internal val providers: Multimap<String, String> = HashMultimap.create()

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        mMessager = processingEnv.messager
        mFiler = processingEnv.filer
        mElements = processingEnv.elementUtils
        mTypes = processingEnv.typeUtils
        mTypeUtils = TypeUtils(mTypes, mElements)

        // Attempt to get user configuration [moduleName]
        val options = processingEnv.options

        if (options.isNotEmpty()) {
            moduleName = options[KEY_MODULE_NAME] ?: ""
        }

        if (moduleName.isEmpty()) {
            error(NO_MODULE_NAME_TIPS)
            throw RuntimeException("Router::Compiler >>> No module name, for more information, look at gradle log.")
        } else {
            moduleName = moduleName.replace("[^0-9a-zA-Z_]+".toRegex(), "")
        }
    }

    private val NO_MODULE_NAME_TIPS = """These no module name, at 'build.gradle', like :
                android {
                    defaultConfig {
                        ...
                        javaCompileOptions {
                            annotationProcessorOptions {
                                arguments = [moduleName: project.getName()]
                            }
                        }
                    }
                }
            """


    internal fun generateConfigFiles() {
        val filer = processingEnv.filer
        for (providerInterface in providers.keySet()) {
            val resourceFile = "META-INF/services/$providerInterface"
            info("Working on resource file: $resourceFile")
            try {
                val allServices: SortedSet<String> = Sets.newTreeSet()
                try {
                    // would like to be able to print the full path
                    // before we attempt to get the resource in case the behavior
                    // of filer.getResource does change to match the spec, but there's
                    // no good way to resolve CLASS_OUTPUT without first getting a resource.
                    val existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceFile)
                    info("Looking for existing resource file at " + existingFile.toUri())
                    val oldServices: Set<String> = ServicesFiles.readServiceFile(existingFile.openInputStream())
                    info("Existing service entries: $oldServices")
                    allServices.addAll(oldServices)
                } catch (e: IOException) {
                    // According to the javadoc, Filer.getResource throws an exception
                    // if the file doesn't already exist.  In practice this doesn't
                    // appear to be the case.  Filer.getResource will happily return a
                    // FileObject that refers to a non-existent file but will throw
                    // IOException if you try to open an input stream for it.
                    info("Resource file did not already exist.")
                }
                val newServices: Set<String> = HashSet(providers[providerInterface])
                if (!allServices.addAll(newServices)) {
                    info("No new service entries being added.")
                    continue
                }
                info("New service file contents: $allServices")
                val fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFile)
                fileObject.openOutputStream().use { out -> ServicesFiles.writeServiceFile(allServices, out) }
                info("Wrote to: " + fileObject.toUri())
            } catch (e: IOException) {
                error("Unable to create $resourceFile, $e")
                return
            }
        }
    }

    private fun error(error: String?) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, this.javaClass.canonicalName + " : " + error)
    }

    private fun info(error: String) {
        mMessager.printMessage(Diagnostic.Kind.WARNING, this.javaClass.canonicalName + " : " + error)
    }

    companion object {
        const val KEY_MODULE_NAME = "moduleName"
        const val ROUTER_INTERFACE_PATH = "com.mm.router.IRouterRulesCreator"
    }

}