package engineer.kodai.yanniang

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val name: Flow<String>
    suspend fun setName(newName: String): Unit
}

val NAME_KEY = stringPreferencesKey("NAME_KEY")

class SettingsRepositoryImpl(private val dataStore: DataStore<Preferences>) : SettingsRepository {
    override val name = dataStore.data.map { it[NAME_KEY] ?: "" }

    override suspend fun setName(newName: String) {
        dataStore.edit {
            it[NAME_KEY] = newName
        }
    }
}
