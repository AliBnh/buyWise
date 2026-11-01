import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api/client';
import { AssistantChatResponse, CitedReference } from '../types';
import {
  Sparkles,
  Send,
  Bot,
  User,
  ExternalLink,
  ShieldCheck,
  AlertCircle,
  HelpCircle,
} from 'lucide-react';

interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  content: string;
  citations?: CitedReference[];
  timestamp: string;
}

export const AiAssistantPage: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      content:
        'Hello! I am your BuyWise Purchase Intelligence Assistant. I am grounded in your authentic purchasing records, active comparisons, warranties, and return deadlines. How can I help you today?',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [input, setInput] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const samplePrompts = [
    'Which laptop offer provides better value in my comparison?',
    'What is the return deadline for my LG refrigerator?',
    'Which of my appliances still have active warranties?',
    'Summarize the warranty conditions and exclusions on my invoice.',
  ];

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async (textToSend?: string) => {
    const text = textToSend || input;
    if (!text.trim() || submitting) return;

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      sender: 'user',
      content: text.trim(),
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMessage]);
    if (!textToSend) setInput('');
    setSubmitting(true);

    try {
      const data = await api.post<AssistantChatResponse>('/ai/assistant/chat', {
        message: text.trim(),
      });

      const assistantMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        content: data.response,
        citations: data.citedReferences,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };

      setMessages((prev) => [...prev, assistantMessage]);
    } catch (err: any) {
      const errorMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        content:
          'I apologize, but I could not reach the AI inference service right now: ' +
          (err.message || 'Unknown error') +
          '. Your database records remain secure.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-4 h-[calc(100vh-7rem)] flex flex-col">
      {/* Header */}
      <div className="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-slate-900 text-emerald-400 flex items-center justify-center">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-sm sm:text-base font-bold text-slate-900">
              BuyWise AI Purchase Assistant
            </h1>
            <p className="text-[11px] text-slate-500">
              Context-aware answers strictly grounded in your authentic BuyWise data
            </p>
          </div>
        </div>

        <span className="hidden sm:inline-flex items-center gap-1.5 px-2.5 py-1 bg-emerald-50 text-emerald-800 border border-emerald-200 rounded-lg text-xs font-semibold">
          <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
          Multi-Tenant Isolated
        </span>
      </div>

      {/* Chat Thread */}
      <div className="flex-1 bg-white rounded-2xl border border-slate-200 p-4 sm:p-6 shadow-xs overflow-y-auto space-y-4">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex items-start gap-3 ${
              msg.sender === 'user' ? 'flex-row-reverse' : ''
            }`}
          >
            <div
              className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 text-xs font-semibold ${
                msg.sender === 'user'
                  ? 'bg-slate-900 text-white'
                  : 'bg-emerald-100 text-emerald-800'
              }`}
            >
              {msg.sender === 'user' ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
            </div>

            <div
              className={`max-w-[85%] sm:max-w-[75%] rounded-2xl p-4 text-xs leading-relaxed ${
                msg.sender === 'user'
                  ? 'bg-slate-900 text-white rounded-tr-none'
                  : 'bg-slate-50 border border-slate-200 text-slate-800 rounded-tl-none'
              }`}
            >
              <p className="whitespace-pre-wrap">{msg.content}</p>

              {/* Traceable Citations */}
              {msg.citations && msg.citations.length > 0 && (
                <div className="mt-3 pt-2.5 border-t border-slate-200/80">
                  <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wide block mb-1.5">
                    Cited Database Records:
                  </span>
                  <div className="flex flex-wrap gap-1.5">
                    {msg.citations.slice(0, 3).map((cite, i) => (
                      <span
                        key={i}
                        className="inline-flex items-center gap-1 px-2 py-0.5 bg-white border border-slate-200 rounded-md text-[10px] text-slate-700 font-medium"
                      >
                        <span className="font-semibold text-slate-900">[{cite.resourceType}]</span>
                        {cite.title} ({cite.snippet})
                      </span>
                    ))}
                  </div>
                </div>
              )}

              <span
                className={`block text-[10px] mt-1.5 ${
                  msg.sender === 'user' ? 'text-slate-400 text-right' : 'text-slate-400'
                }`}
              >
                {msg.timestamp}
              </span>
            </div>
          </div>
        ))}

        {submitting && (
          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-xl bg-emerald-100 text-emerald-800 flex items-center justify-center shrink-0">
              <Bot className="w-4 h-4" />
            </div>
            <div className="bg-slate-50 border border-slate-200 rounded-2xl p-3 text-xs text-slate-500 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              Searching purchases, comparing specifications, and consulting deadlines...
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Suggested Prompts */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs shrink-0">
        <span className="text-[11px] font-semibold text-slate-400 whitespace-nowrap">
          Suggestions:
        </span>
        {samplePrompts.map((prompt, i) => (
          <button
            key={i}
            onClick={() => handleSendMessage(prompt)}
            disabled={submitting}
            className="px-3 py-1.5 bg-white hover:bg-slate-50 border border-slate-200 rounded-xl text-slate-700 whitespace-nowrap text-[11px] font-medium shadow-2xs transition-colors shrink-0 disabled:opacity-50"
          >
            {prompt}
          </button>
        ))}
      </div>

      {/* Input Box */}
      <div className="bg-white p-3 rounded-2xl border border-slate-200 shadow-xs shrink-0">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSendMessage();
          }}
          className="flex items-center gap-2"
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            disabled={submitting}
            placeholder="Ask about your purchases, warranty coverage, or offer trade-offs..."
            className="flex-1 text-xs px-3 py-2 border-0 focus:outline-hidden focus:ring-0 text-slate-900 bg-transparent"
          />

          <button
            type="submit"
            disabled={submitting || !input.trim()}
            className="p-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl transition-colors disabled:opacity-40 shrink-0"
            title="Send Message"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};
