import api from '../../config/api';

export interface RoomData {
  id?: number;
  name: string;
  roomTypeId: string;
  status: 'ACTIVE' | 'MAINTENANCE' | 'INACTIVE';
  templateFileName?: string;
  rows?: number;
  cols?: number;
  branchId?: number | null;
  capacity?: number;
  supportedFormats?: string[];
  hasShowtime?: boolean;
  seats?: any[];
}

export const adminRoomService = {
  getRooms: async (branchId?: number) => {
    let url = '/rooms';
    if (branchId) {
      url += `?branchId=${branchId}`;
    }
    const res = await api.get(url);
    return res.data;
  },

  getRoomDetails: async (id: number) => {
    const res = await api.get(`/rooms/${id}`);
    return res.data;
  },

  createRoom: async (data: RoomData) => {
    const res = await api.post('/rooms', data);
    return res.data;
  },

  updateRoom: async (id: number, data: RoomData) => {
    const res = await api.put(`/rooms/${id}`, data);
    return res.data;
  },

  deleteRoom: async (id: number) => {
    const res = await api.delete(`/rooms/${id}`);
    return res.data;
  },

  getRoomTypes: async () => {
    const res = await api.get('/public/room-types');
    return res.data;
  },

  getRoomTemplates: async () => {
    const res = await api.get('/admin/room-templates');
    return res.data;
  },

  getTemplateDetails: async (fileName: string) => {
    const res = await api.get(`/admin/room-templates/${fileName}`);
    return res.data;
  },

  saveTemplate: async (data: { templateName: string; roomTypeId: string; rows: number; cols: number; seats: any[] }) => {
    const res = await api.post('/admin/room-templates', data);
    return res.data;
  },

  updateRoomLayout: async (id: number, data: { rows: number; cols: number; seats: any[] }) => {
    const res = await api.put(`/rooms/${id}/layout`, data);
    return res.data;
  },
};
