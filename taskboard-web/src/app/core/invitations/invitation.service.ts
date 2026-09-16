import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";

export interface Invitation {
    id: number;
    boardId: number;
    boardTitle: string;
    inviteeUserId: number;
    inviteeUsername: string;
    invitedByUsername: string;
    status: string;
    createdAt: string;
}

@Injectable({
    providedIn: 'root',
})
export class InvitationService {

    private readonly http = inject(HttpClient);

    listPending() {
        return this.http.get<Invitation[]>('/api/invitations');
    }

    accept(invitationId: number) {
        return this.http.post<Invitation>(`/api/invitations/${invitationId}/accept`, {});
    }

  reject(invitationId: number) {
    return this.http.post<Invitation>(`/api/invitations/${invitationId}/reject`, {});
  }

  cancel(invitationId: number) {
    return this.http.post<Invitation>(`/api/invitations/${invitationId}/cancel`, {});
  }

}