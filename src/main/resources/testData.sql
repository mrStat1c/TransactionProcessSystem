
-- Данные для SPR отчета
use test;
insert into test.rejects(id, file_name, order_number, order_position_number, type, code, incorrect_field_value, record_date)
values  (1, 'file1', 123, null, 'ORDER', '100', 'XXX', '2018-10-05 12:12:12'),
		(2, 'file1', 123, null, 'ORDER', '100', 'XXX', '2018-10-05 12:12:12'),
		(3, 'file1', 123, null, 'ORDER', '101', 'XXX', '2018-10-05 12:12:12'),
        (4, 'file1', 345, null, 'ORDER', '100', 'XXX', '2018-10-05 12:12:12'),
		(5, 'file1', 345, null, 'ORDER', '102', 'XXX', '2018-10-05 12:12:12'),
		(6, 'file1', 123, 1, 'ORDER_POSITION', '200', 'XXX', '2018-10-05 12:12:12'),
		(7, 'file1', 123, 2, 'ORDER_POSITION', '200', 'XXX', '2018-10-05 12:12:12'),
		(8, 'file1', 123, 3, 'ORDER_POSITION', '300', 'XXX', '2018-10-05 12:12:12'),
		(9, 'file1', 345, 1, 'ORDER_POSITION', '200', 'XXX', '2018-10-05 12:12:12'),
		(10, 'file1', 345, 1, 'ORDER_POSITION', '205', 'XXX', '2018-10-05 12:12:12'),
		(11, 'file1', 345, 2, 'ORDER_POSITION', '300', 'XXX', '2018-10-05 12:12:12');
insert into test.orders(id, number, sale_point_id, order_date, record_date, card_id, file_id, ccy_id, sum, rejected, sale_point_order_num)
values (1, 123, 163456, '2018-10-10 12:12:12', '2018-10-10 12:12:12', null, 1, 1, 100, 'Y', 123),
	   (2, 345, 435211, '2018-10-10 12:12:12', '2018-10-10 12:12:12', null, 1, 1, 100, 'Y', 123);