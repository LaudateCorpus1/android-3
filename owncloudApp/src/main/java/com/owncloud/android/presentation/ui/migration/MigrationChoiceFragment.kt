/**
 * ownCloud Android client application
 *
 * @author Abel García de Prada
 * <p>
 * Copyright (C) 2021 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.presentation.ui.migration

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.owncloud.android.R
import com.owncloud.android.presentation.viewmodels.migration.MigrationState
import com.owncloud.android.presentation.viewmodels.migration.MigrationViewModel
import com.owncloud.android.utils.DisplayUtils.bytesToHumanReadable
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MigrationChoiceFragment : Fragment(R.layout.fragment_migration_choice) {

    private val migrationViewModel: MigrationViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val migrationChoiceState = migrationViewModel.migrationState.value?.peekContent() as MigrationState.MigrationChoiceState

        view.findViewById<TextView>(R.id.migration_choice_subtitle)?.apply {
            text = getString(
                R.string.scoped_storage_wizard_explanation,
                bytesToHumanReadable(migrationChoiceState.legacyStorageSpaceInBytes, context)
            )
        }

        view.findViewById<Button>(R.id.migration_choice_complete_button)?.setOnClickListener {
            migrationViewModel.moveToNextState(MigrationState.MigrationType.MIGRATE_AND_CLEAN)
        }

        view.findViewById<Button>(R.id.migration_choice_partial_button)?.apply {
            if (migrationViewModel.isThereEnoughSpaceInDevice()) {
                setOnClickListener {
                    migrationViewModel.moveToNextState(MigrationState.MigrationType.MIGRATE_AND_KEEP)
                }
            } else {
                visibility = View.GONE
                view.findViewById<TextView>(R.id.migration_choice_complete_button)?.text =
                    getString(R.string.scoped_storage_wizard_migrate_and_clean_button_without_recommended)
            }
        }
    }
}
