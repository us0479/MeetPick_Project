/**
 * <h2>A — Invitation</h2>
 * Redis TTL 기반 1회용 초대 Token의 생성/조회/소비를 구현한다.
 *
 * <p>Owner: A
 * <p>주의: Invitation 관계형 테이블은 현재 설계에 없다. 참여 성공 시 B의 Participant를 생성한다.
 */
package com.meetpick.invitation;
