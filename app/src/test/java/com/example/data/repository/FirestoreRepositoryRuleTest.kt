package com.example.data.repository

import com.example.data.model.CloudDesignProject
import com.example.data.model.UserProfile
import com.example.data.util.LayerSerializer
import com.example.data.util.OperationType
import com.example.model.CanvasBlendMode
import com.example.model.TextFontFamily
import com.example.model.TextLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreRepositoryRuleTest {

    @Test
    fun testCloudDesignProjectModelConstraints() {
        val project = CloudDesignProject(
            id = "proj_123",
            userId = "user_alice",
            title = "Neon Glow Poster",
            canvasWidth = 1080L,
            canvasHeight = 1080L,
            presetName = "Square 1:1",
            backgroundColor = 0xFF12141AL,
            layerCount = 5L,
            layersJson = "[]"
        )

        assertEquals("proj_123", project.id)
        assertEquals("user_alice", project.userId)
        assertEquals("Neon Glow Poster", project.title)
        assertEquals(1080L, project.canvasWidth)
        assertEquals(1080L, project.canvasHeight)
        assertEquals(5L, project.layerCount)
    }

    @Test
    fun testUserProfileModelConstraints() {
        val user = UserProfile(
            id = "user_alice",
            userId = "user_alice",
            displayName = "Alice Designer",
            email = "alice@example.com",
            projectCount = 3L
        )

        assertEquals("user_alice", user.userId)
        assertEquals("Alice Designer", user.displayName)
        assertEquals("alice@example.com", user.email)
        assertEquals(3L, user.projectCount)
    }

    @Test
    fun testLayerSerializationRoundTrip() {
        val textLayer = TextLayer(
            id = "layer_1",
            name = "Title Layer",
            text = "PHOTOSTUDIO",
            fontSize = 48f,
            textColor = 0xFF00C7FFL,
            fontFamily = TextFontFamily.IMPACT,
            isBold = true,
            blendMode = CanvasBlendMode.MULTIPLY
        )

        val json = LayerSerializer.serialize(listOf(textLayer))
        assertNotNull(json)
        assertTrue(json.contains("PHOTOSTUDIO"))
        assertTrue(json.contains("MULTIPLY"))

        val deserialized = LayerSerializer.deserialize(json)
        assertEquals(1, deserialized.size)
        val restored = deserialized[0] as TextLayer
        assertEquals("PHOTOSTUDIO", restored.text)
        assertEquals(CanvasBlendMode.MULTIPLY, restored.blendMode)
        assertEquals(TextFontFamily.IMPACT, restored.fontFamily)
    }

    @Test
    fun testOperationTypes() {
        assertEquals("create", OperationType.CREATE.value)
        assertEquals("update", OperationType.UPDATE.value)
        assertEquals("delete", OperationType.DELETE.value)
        assertEquals("list", OperationType.LIST.value)
        assertEquals("get", OperationType.GET.value)
        assertEquals("write", OperationType.WRITE.value)
    }
}
