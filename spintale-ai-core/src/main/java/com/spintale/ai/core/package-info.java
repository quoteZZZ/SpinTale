/**
 * SpinTale AI Core Module
 * 
 * <p>Core abstractions, data models, and SPI interfaces for the AI module.</p>
 * 
 * <p>This module provides:</p>
 * <ul>
 *   <li>Data models: {@link com.spintale.ai.core.model.ChatMessage}, 
 *       {@link com.spintale.ai.core.model.ChatRequest}, 
 *       {@link com.spintale.ai.core.model.ChatResponse}</li>
 *   <li>SPI interfaces: {@link com.spintale.ai.core.spi.ChatModel}, 
 *       {@link com.spintale.ai.core.spi.EmbeddingModel}, 
 *       {@link com.spintale.ai.core.spi.ModelProvider}</li>
 *   <li>Provider registry and routing: {@link com.spintale.ai.core.spi.ModelProviderRegistry},
 *       {@link com.spintale.ai.core.spi.ModelRouter}</li>
 *   <li>Configuration options: {@link com.spintale.ai.core.options.ChatOptions}</li>
 *   <li>Exception handling: {@link com.spintale.ai.core.exception.AiServiceException}</li>
 * </ul>
 * 
 * @since 3.9.2
 */
package com.spintale.ai.core;
