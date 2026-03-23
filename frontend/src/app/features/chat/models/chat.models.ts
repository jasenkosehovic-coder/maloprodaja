export interface ChatMessage {
  id: string;
  senderUsername: string;
  senderName: string;
  poslovnicaId: number;
  poslovnicaNaziv: string;
  sadrzaj: string;
  timestamp: string;
  tip: 'TEXT' | 'SYSTEM';
}

export interface SendMessageRequest {
  sadrzaj: string;
}

export interface ChatRoom {
  id: string;
  naziv: string;
  tip: 'GLOBALNI' | 'POSLOVNICA' | 'DIREKTNI';
  neprocitano: number;
}
