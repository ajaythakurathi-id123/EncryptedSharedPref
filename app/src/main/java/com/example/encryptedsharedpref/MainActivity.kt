package com.example.encryptedsharedpref

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.encryptedsharedpref.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var filePath: String
    private val fileNameEncrypt = "my_sensitive_data"
    private val fileName = "open_data"

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val prefFile = File("/data/data/${BuildConfig.APPLICATION_ID}/shared_prefs/$fileName.xml")
        ///data/data/com.example.encryptedsharedpref/shared_prefs
//        filePath = "${applicationContext.dataDir}/shared_pref"
        filePath = "/data/data/${BuildConfig.APPLICATION_ID}/shared_prefs"
        pref = getSharedPreferences(fileName, Context.MODE_PRIVATE)

        binding.btnRead.setOnClickListener(this)
        binding.btnWrite.setOnClickListener(this)
        binding.btnMigrate.setOnClickListener(this)

        Log.d(TAG, "onCreate: path: ${applicationContext.filesDir.path}")


    }

    private fun getPrefFile(): SharedPreferences {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        return EncryptedSharedPreferences.create(
            fileNameEncrypt, // fileName
            mainKeyAlias, // masterKeyAlias
            this, // context
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // prefKeyEncryptionScheme
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // prefvalueEncryptionScheme
        )
    }

    private fun migrate() {
        val allEntry: Map<String, *> = pref.all

        allEntry.entries.forEach { entry ->
            val key: String = entry.key

            when (val value: Any? = entry.value) {
                is Int -> {
                    //add to encrypted file
                    getPrefFile().edit().apply {
                        putInt(key, value)
                    }.apply()
                    //remove from old file
                    pref.edit().remove(key).apply()
                }
                is Boolean -> {
                    //add to encrypted file
                    getPrefFile().edit().apply {
                        putBoolean(key, value)
                    }.apply()
                    //remove from old file
                    pref.edit().remove(key).apply()
                }
                is Long -> {
                    //add to encrypted file
                    getPrefFile().edit().apply {
                        putLong(key, value)
                    }.apply()
                    //remove from old file
                    pref.edit().remove(key).apply()
                }
                is Float -> {
                    //add to encrypted file
                    getPrefFile().edit().apply {
                        putFloat(key, value)
                    }.apply()
                    //remove from old file
                    pref.edit().remove(key).apply()
                }
                else -> {
                    //add to encrypted file
                    getPrefFile().edit().apply {
                        putString(key, value.toString())
                    }.apply()
                    //remove from old file
                    pref.edit().remove(key).apply()
                }
            }
        }
        //delete pref file
        ///data/data/com.example.encryptedsharedpref/shared_prefs/open_data.xml
        val prefFile = File("/data/data/${BuildConfig.APPLICATION_ID}/shared_prefs/$fileName.xml")
        if (prefFile.exists()) {
            prefFile.delete()
        }

        //rename
        val tempFile = File("/data/data/${BuildConfig.APPLICATION_ID}/shared_prefs/$fileName.xml")

        val encFile = File("/data/data/${BuildConfig.APPLICATION_ID}/shared_prefs/$fileNameEncrypt.xml")

//        encFile.renameTo(tempFile)
//        tempFile.delete()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            binding.btnWrite.id -> {

                val editor = pref.edit()
                editor.putString("stringVal", "Hello World")
                editor.putFloat("floatVal", 1.234F)
                editor.putInt("intVal", 123)
                editor.putLong("longVal", 123456)
                editor.putBoolean("boolVal", true)
                editor.apply()
                //encrypted
                /*getPrefFile().edit().apply {
                    putString("name", "T-Rex")
                }.apply()*/
            }
            binding.btnRead.id -> {
                /*val name: String? = if (migrated) {
                    //encrypted
                    getPrefFile().getString("name", "")
                } else {
                    pref.getString("name", "")
                }

                Log.d(TAG, "onClick: btnRead: ${name ?: "null"}")*/

                val allEntry: Map<String, *> = getPrefFile().all

                allEntry.entries.forEach { entry ->
                    val key: String = entry.key
                    val value: Any? = entry.value

                    Log.d(TAG, "onClick: btnRead: $key || ${value ?: "null"}")
                }
            }
            binding.btnMigrate.id -> {
                migrate()
            }
        }
    }
}