/**
 * ownCloud Android client application
 *
 * @author Abel García de Prada
 * Copyright (C) 2020 ownCloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.data.files.datasources.implementation

import com.owncloud.android.data.files.datasources.LocalFileDataSource
import com.owncloud.android.data.files.datasources.mapper.OCFileMapper
import com.owncloud.android.data.files.db.FileDao
import com.owncloud.android.domain.files.model.MIME_DIR
import com.owncloud.android.domain.files.model.MIME_PREFIX_IMAGE
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.model.OCFile.Companion.ROOT_PARENT_ID
import com.owncloud.android.domain.files.model.OCFile.Companion.ROOT_PATH

class OCLocalFileDataSource(
    private val fileDao: FileDao,
    private val ocFileMapper: OCFileMapper
) : LocalFileDataSource {

    override fun getFileById(fileId: Long): OCFile? =
        ocFileMapper.toModel(fileDao.getFileById(fileId))

    override fun getFileByRemotePath(remotePath: String, owner: String): OCFile? {
        ocFileMapper.toModel(fileDao.getFileByOwnerAndRemotePath(owner, remotePath))?.let { return it }

        // If root folder do not exists, create and return it.
        if (remotePath == ROOT_PATH) {
            val rootFolder = OCFile(
                parentId = ROOT_PARENT_ID,
                owner = owner,
                remotePath = ROOT_PATH,
                length = 0,
                mimeType = MIME_DIR,
                modificationTimestamp = 0
            )
            fileDao.mergeRemoteAndLocalFile(ocFileMapper.toEntity(rootFolder)!!).also { return getFileById(it) }
        }

        return null
    }

    override fun getFolderContent(folderId: Long): List<OCFile> =
        fileDao.getFolderContent(folderId = folderId).map {
            ocFileMapper.toModel(it)!!
        }

    override fun getFolderImages(folderId: Long): List<OCFile> =
        fileDao.getFolderByMimeType(folderId = folderId, mimeType = MIME_PREFIX_IMAGE).map {
            ocFileMapper.toModel(it)!!
        }

    override fun moveFile(sourceFile: OCFile, targetFile: OCFile, finalRemotePath: String, finalStoragePath: String) =
        fileDao.moveFile(
            sourceFile = ocFileMapper.toEntity(sourceFile)!!,
            targetFile = ocFileMapper.toEntity(targetFile)!!,
            finalRemotePath = finalRemotePath,
            finalStoragePath = sourceFile.storagePath?.let { finalStoragePath }
        )

    override fun saveFilesInFolder(listOfFiles: List<OCFile>, folder: OCFile) {
        // Insert first folder container
        // TODO: If it is root, add 0 as parent Id
        val folderId = fileDao.mergeRemoteAndLocalFile(ocFileMapper.toEntity(folder)!!)

        // Then, insert files inside
        listOfFiles.forEach {
            // Add parent id to each file
            fileDao.mergeRemoteAndLocalFile(ocFileMapper.toEntity(it)!!.apply { parentId = folderId })
        }
    }

    override fun saveFile(file: OCFile) {
        fileDao.mergeRemoteAndLocalFile(ocFileMapper.toEntity(file)!!)
    }

    override fun removeFile(fileId: Long) {
        fileDao.deleteFileWithId(fileId)
    }
}
